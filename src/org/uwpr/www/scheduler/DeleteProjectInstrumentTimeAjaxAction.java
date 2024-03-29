/**
 * DeleteProjectInstrumentTimeAjaxAction.java
 * @author Vagisha Sharma
 * May 28, 2011
 */
package org.uwpr.www.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;
import org.uwpr.instrumentlog.*;
import org.uwpr.scheduler.UsageBlockDeletableDecider;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class DeleteProjectInstrumentTimeAjaxAction extends Action {

	private static final Logger log = LogManager.getLogger(DeleteProjectInstrumentTimeAjaxAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}
		
        
		PrintWriter responseWriter = response.getWriter();
		response.setContentType("text/plain");
		
        // we need a projectID
        int projectId = 0;
        try {
        	projectId = Integer.parseInt(request.getParameter("projectId"));
        }
        catch(NumberFormatException e) {
        	projectId = 0;
        }
        if(projectId == 0) {
        	responseWriter.write("ERROR: Invalid projectID: "+projectId+" in request");
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	return null;
        }
        
        // Make sure the user has access to the project
        Project project = null;
        try {
        	project = ProjectFactory.getProject(projectId);
        	if(!project.checkAccess(user.getResearcher())) {
        		responseWriter.write("ERROR: User does not have access to delete instrument time for project");
        		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            	return null;
        	}
        }
        catch(Exception e) {
        	log.error("Error checking access to project ID: "+projectId, e);
        	responseWriter.write("ERROR: Error loading project to check access. "+e.getMessage());
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	return null;
        }
        
        // get the usage block ID(s)
        List<Integer> usageBlockIds = new ArrayList<Integer>();
        String usageBlockIdString = request.getParameter("usageBlockIds");
        if(usageBlockIdString == null) {
        	responseWriter.write("ERROR: No usage block IDs found in the request");
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        String[] tokens = usageBlockIdString.split(",");
        for(String token: tokens) {

        	try {
        		usageBlockIds.add(Integer.parseInt(token));
        	}
        	catch(NumberFormatException e) {
        		responseWriter.write("ERROR: Invalid usageBlockId: "+token+" in request");
            	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	}
        }
        
        if(usageBlockIds.size() == 0) {
        	responseWriter.write("ERROR: Invalid usageBlockId: "+request.getParameter("usageBlockId")+" in request");
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	
        	return null;
        }
        
        List<Integer> blockIdsToDelete = new ArrayList<Integer>();

		Integer instrumentId = null;
		List<UsageBlockBase> usageBlocks = new ArrayList<UsageBlockBase>();
        for(int usageBlockId: usageBlockIds) {
        	
        	UsageBlockBase usageBlock = UsageBlockBaseDAO.getUsageBlockBase(usageBlockId);
        	if(usageBlock == null) {
        		responseWriter.write("ERROR: No usage block found for usageBlockId: "+usageBlockId);
        		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        		return null;
        	}

			usageBlocks.add(usageBlock);

        	StringBuilder errorMessage = new StringBuilder();
        	if(!UsageBlockDeletableDecider.getInstance().isBlockDeletable(usageBlock, user, errorMessage)) {
        		responseWriter.write(errorMessage.toString());
        		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        		return null;
        	}

			log.info("Deleting usage block - Researcher " + user.getIdAndName() + "; " + usageBlock.toString());
			blockIdsToDelete.add(usageBlockId);
			instrumentId = usageBlock.getInstrumentID();
        }

		// This action is called via the calendar.  The requested blocks will be deleted.
		// 10.28.2022 - We don't have to keep the deleted blocks in the database since we are no longer billing
		// sign-up cost for deleted blocks.
		for (UsageBlockBase usageBlock: usageBlocks)
		{
			log.info("Deleting usage block: Researcher: " + user.getIdAndName() + "; " + usageBlock.toString());
			InstrumentUsageDAO.getInstance().purge(usageBlock, user.getResearcher());
		}


		// Email admins
		boolean emailProjectResearchers = !Groups.getInstance().isAdministrator(user.getResearcher());
		MsInstrument instrument = MsInstrumentUtils.instance().getMsInstrument(instrumentId);
		ProjectInstrumentUsageUpdateEmailer.getInstance().sendEmail(project, instrument, user.getResearcher(),
				usageBlocks, null,
				ProjectInstrumentUsageUpdateEmailer.Action.DELETED, null, emailProjectResearchers);

        PrintWriter writer = response.getWriter();
        writer.write("SUCCESS");
        return null;
	}
}
