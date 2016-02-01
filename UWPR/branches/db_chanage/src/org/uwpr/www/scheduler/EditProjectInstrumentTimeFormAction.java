/**
 * EditProjectInstrumentTimeFormAction.java
 * @author Vagisha Sharma
 * Jan 6, 2012
 */
package org.uwpr.www.scheduler;

import org.apache.struts.action.*;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.uwpr.instrumentlog.UsageBlockBase;
import org.uwpr.instrumentlog.UsageBlockBaseDAO;
import org.uwpr.scheduler.UsageBlockDeletableDecider;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 */
public class EditProjectInstrumentTimeFormAction extends Action {

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
		
		// we need a projectID
        int projectId = 0;
        try {
        	projectId = Integer.parseInt(request.getParameter("projectId"));
        }
        catch(NumberFormatException e) {
        	projectId = 0;
        }
        if(projectId == 0) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.scheduler.invalidid", "Invalid projectID in request."));
            saveErrors( request, errors );
            return mapping.findForward("standardHome");
        }
		
        // Make sure the user has access to the project
        Project project = null;
        try {
        	project = ProjectFactory.getProject(projectId);
        	
        	if(project == null) {
        		ActionErrors errors = new ActionErrors();
                errors.add("scheduler", new ActionMessage("error.scheduler.invalidid", "Project with ID: "+projectId+" not found in the database."));
                saveErrors( request, errors );
                return mapping.findForward("standardHome");
        	}
        }
        catch(Exception e) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.costcenter.load", e.getMessage()));
            saveErrors( request, errors );
            return mapping.findForward("standardHome");
        }
        try {
        	
        	if(!project.checkAccess(user.getResearcher())) {
        		ActionErrors errors = new ActionErrors();
                errors.add("scheduler", new ActionMessage("error.costcenter.invalidaccess", 
                		"User does not have access to edit instrument time for project "+projectId+"."));
                saveErrors( request, errors );
                ActionForward fwd = mapping.findForward("viewProject");
                return new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
        	}
        }
        catch(Exception e) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.costcenter.load", 
            		"Project ID: "+projectId+". ERROR: "+e.getMessage()));
            saveErrors( request, errors );
            ActionForward fwd = mapping.findForward("viewProject");
            return new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
        }
        
        // we need an instrumentID
        int instrumentId = 0;
        try {
        	instrumentId = Integer.parseInt(request.getParameter("instrumentId"));
        }
        catch(NumberFormatException e) {
        	instrumentId = 0;
        }
        if(instrumentId == 0) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.scheduler.invalidid", "Invalid instrumentID in request"));
            saveErrors( request, errors );
            ActionForward fwd = mapping.findForward("viewProject");
            return new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
        }
        MsInstrument instrument = null;
        try {
        	instrument = MsInstrumentUtils.instance().getMsInstrument(instrumentId);
        	if(instrument == null) {
        		ActionErrors errors = new ActionErrors();
                errors.add("scheduler", new ActionMessage("error.scheduler.invalidid", "Instrument with ID "+instrumentId+" not found in the database."));
                saveErrors( request, errors );
                ActionForward fwd = mapping.findForward("viewProject");
                return new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
        	}
        }
        catch(Exception e) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.costcenter.load", 
            		"Instrument ID: "+projectId+". ERROR: "+e.getMessage()));
            saveErrors( request, errors );
            ActionForward fwd = mapping.findForward("viewProject");
            return new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
        }
		
		// get the usageBlockIds from the request
		// get the usage block ID(s)
        List<Integer> usageBlockIds = new ArrayList<Integer>();
        String usageBlockIdString = request.getParameter("usageBlockIds");
        if(usageBlockIdString != null) {
        	String[] tokens = usageBlockIdString.split(",");
            for(String token: tokens) {

            	try {
            		usageBlockIds.add(Integer.parseInt(token));
            	}
            	catch(NumberFormatException e) {
            		ActionErrors errors = new ActionErrors();
                    errors.add("scheduler", new ActionMessage("error.costcenter.invaliddata", 
                    		"Invalid usage block ID found in request: "+token+"."));
                    saveErrors( request, errors );
                    ActionForward fwd = mapping.findForward("viewScheduler");
                    return new ActionForward(fwd.getPath()+"?projectId="+projectId+"&instrumentId="+instrumentId, fwd.getRedirect());
            	}
            }
        }
        if(usageBlockIds.size() == 0) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.costcenter.invaliddata", 
            		"No usage block IDs found in request."));
            saveErrors( request, errors );
            return mapping.findForward("standardHome");
        }
        
        
        // load the usage blocks
        List<UsageBlockBase> blocksToEdit = new ArrayList<UsageBlockBase>(usageBlockIds.size());
        for(int usageBlockId: usageBlockIds) {
        	
        	UsageBlockBase usageBlock = UsageBlockBaseDAO.getUsageBlockBase(usageBlockId);
        	if(usageBlock == null) {
        		ActionErrors errors = new ActionErrors();
                errors.add("scheduler", new ActionMessage("error.costcenter.invaliddata", 
                		"No usage block found for usageBlockId: "+usageBlockId));
                saveErrors( request, errors );
                ActionForward fwd = mapping.findForward("viewScheduler");
                return new ActionForward(fwd.getPath()+"?projectId="+projectId+"&instrumentId="+instrumentId, fwd.getRedirect());
        	}
        	blocksToEdit.add(usageBlock);
        }
        
        // get the project for the given usage blocks.  They should all be for the same project.
        // the instrumentID form the blocks should also be the same
        for(UsageBlockBase block: blocksToEdit) {
        	
        	int blkProjId = block.getProjectID();
        	if(blkProjId != projectId) {
        		ActionErrors errors = new ActionErrors();
                errors.add("scheduler", new ActionMessage("error.costcenter.invaliddata", 
                		"Given usage block ( "+block.getID()+") is not for project "+projectId));
                saveErrors( request, errors );
                ActionForward fwd = mapping.findForward("viewScheduler");
                return new ActionForward(fwd.getPath()+"?projectId="+projectId+"&instrumentId="+instrumentId, fwd.getRedirect());
        	}
        	
        	int blkInstrId = block.getInstrumentID();
        	if(blkInstrId != instrumentId) {
        		ActionErrors errors = new ActionErrors();
                errors.add("scheduler", new ActionMessage("error.costcenter.invaliddata", 
                		"Given usage block ( "+block.getID()+") is not for instrument "+projectId));
                saveErrors( request, errors );
                ActionForward fwd = mapping.findForward("viewScheduler");
                return new ActionForward(fwd.getPath()+"?projectId="+projectId+"&instrumentId="+instrumentId, fwd.getRedirect());
        	}
        }
        
        // Make sure the old blocks can be deleted.
        for(UsageBlockBase block: blocksToEdit) {
        	StringBuilder errorMessage = new StringBuilder();
        	if(!UsageBlockDeletableDecider.getInstance().isBlockDeletable(block, user, errorMessage)) {
        		ActionErrors errors = new ActionErrors();
                errors.add("scheduler", new ActionMessage("error.costcenter.delete", 
                		"Block ID "+block.getID()+": "+block.getStartDateFormated()+" - "+block.getEndDateFormated()+
                		". "+errorMessage.toString()));
                saveErrors( request, errors );
                ActionForward fwd = mapping.findForward("viewScheduler");
                return new ActionForward(fwd.getPath()+"?projectId="+projectId+"&instrumentId="+instrumentId, fwd.getRedirect());
        	}
        }
        
        // sort the blocks by start date/time
        Collections.sort(blocksToEdit, new Comparator<UsageBlockBase>() {
			@Override
			public int compare(UsageBlockBase blk1, UsageBlockBase blk2) {
				return blk1.getStartDate().compareTo(blk2.getStartDate());
			}
        	
		});
        
        // TODO Multiple creators??
        Researcher creator = new Researcher();
        creator.load(blocksToEdit.get(0).getResearcherID());
        Researcher updater = null;
        if(blocksToEdit.get(0).getUpdaterResearcherID() != 0) {
        	updater = new Researcher();
        	updater.load(blocksToEdit.get(0).getUpdaterResearcherID());
        }
        
        EditProjectInstrumentTimeForm editForm = new EditProjectInstrumentTimeForm();
        SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat timeFmt = new SimpleDateFormat("H");
        editForm.setStartDate(dateFmt.format(blocksToEdit.get(0).getStartDate()));
        editForm.setEndDate(dateFmt.format(blocksToEdit.get(blocksToEdit.size() - 1).getEndDate()));
        editForm.setStartTime(timeFmt.format(blocksToEdit.get(0).getStartDate()));
        editForm.setEndTime(timeFmt.format(blocksToEdit.get(blocksToEdit.size() - 1).getEndDate()));
        editForm.setProjectId(projectId);
        editForm.setInstrumentId(instrumentId);
        editForm.setInstrumentOperatorId(blocksToEdit.get(0).getInstrumentOperatorId());
        editForm.setInstrumentName(instrument.getName());
        editForm.setUsageBlockIdsToEdit(usageBlockIdString);
        
        // TODO Multiple creators??
        editForm.setCreatorId(creator.getID());
        dateFmt = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
        editForm.setCreateDate(dateFmt.format(blocksToEdit.get(0).getDateCreated()));
        if(updater != null) {
        	editForm.setUpdaterId(updater.getID());
        	editForm.setUpdateDate(dateFmt.format(blocksToEdit.get(0).getDateChanged()));
        }
        
        request.setAttribute("editInstrumentTimeForm", editForm);
        request.getSession().setAttribute("startTimeOptions", TimeOption.getStartTimeOptions(user));
        request.getSession().setAttribute("endTimeOptions", TimeOption.getEndTimeOptions(user));
        
        List<Researcher> instrumentOperators = Groups.getInstance().getMembers(Groups.INSTRUMENT_OPERATOR);
        request.setAttribute("instrumentOperators", project.getInstrumentOperators(instrumentOperators));

        return mapping.findForward("Success");
	}
}
