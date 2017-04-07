/**
 * EditProjectInstrumentTimeFormAction.java
 * @author Vagisha Sharma
 * Jan 6, 2012
 */
package org.uwpr.www.scheduler;

import org.apache.struts.action.*;
import org.uwpr.costcenter.InvoiceInstrumentUsage;
import org.uwpr.costcenter.InvoiceInstrumentUsageDAO;
import org.uwpr.instrumentlog.*;
import org.yeastrc.project.*;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.project.payment.ProjectPaymentMethodDAO;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 */
public class EditBlockDetailsFormAction extends Action {

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
        List<UsageBlockBase> blocksToUpdate = new ArrayList<UsageBlockBase>(usageBlockIds.size());
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
            blocksToUpdate.add(usageBlock);
        }
        
        // get the project for the given usage blocks.  They should all be for the same project
        // and they should all have the same payment methods.
        // Update: 01/20/17 - Blocks need not be from the projectId in the request but they have to be from the same project.
        int blkProjId = blocksToUpdate.get(0).getProjectID();
        for(UsageBlockBase block: blocksToUpdate) {

            if(blkProjId != block.getProjectID()) {
                ActionErrors errors = new ActionErrors();
                errors.add("scheduler", new ActionMessage("error.costcenter.invaliddata",
                        "Usage blocks being updated have to be from the same project. Usage block ( "+block.getID()+") is not for project "+blkProjId));
                saveErrors( request, errors );
                ActionForward fwd = mapping.findForward("viewScheduler");
                return new ActionForward(fwd.getPath()+"?projectId="+projectId+"&instrumentId="+instrumentId, fwd.getRedirect());
            }
        }

        // sort the blocks by start date/time
        Collections.sort(blocksToUpdate, new Comparator<UsageBlockBase>() {
            @Override
            public int compare(UsageBlockBase blk1, UsageBlockBase blk2) {
                return blk1.getStartDate().compareTo(blk2.getStartDate());
            }
        });

        UsageBlockBase firstBlock = blocksToUpdate.get(0);

        // If the first block in the range has already been billed, throw an error message.
        InvoiceInstrumentUsage billedBlock = InvoiceInstrumentUsageDAO.getInstance().getInvoiceBlock(firstBlock.getID());
        if(billedBlock != null) {
            ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.costcenter.invalidaccess",
                    "The first block ( "+firstBlock.getStartDateFormated()+" - "+firstBlock.getEndDateFormated()+
                    ") in the selected range has already been billed. Please select blocks that have not been billed."));
            saveErrors( request, errors );
            ActionForward fwd = mapping.findForward("viewScheduler");
            return new ActionForward(fwd.getPath()+"?projectId="+projectId+"&instrumentId="+instrumentId, fwd.getRedirect());
        }

        // get a list of ALL payment methods for the project
        List<PaymentMethod> paymentMethods = ProjectPaymentMethodDAO.getInstance().getCurrentPaymentMethods(projectId);
        request.setAttribute("paymentMethods", paymentMethods);

        List<EditBlockDetailsForm.PaymentPercent> paymentPercentList = new ArrayList<EditBlockDetailsForm.PaymentPercent>();
        if(firstBlock.getProjectID() == projectId)
        {
            // If we are editing blocks that are already assigned to this project,
            // get a list of payment methods used for the first block
            InstrumentUsagePaymentDAO paymentDao = InstrumentUsagePaymentDAO.getInstance();
            List<InstrumentUsagePayment> usagePayments = paymentDao.getPaymentsForUsage(firstBlock.getID());
            for (InstrumentUsagePayment paymentMethod : usagePayments)
            {
                EditBlockDetailsForm.PaymentPercent paymentPercent = new EditBlockDetailsForm.PaymentPercent();
                paymentPercent.setPaymentMethodId(paymentMethod.getPaymentMethod().getId());
                paymentPercent.setPaymentPercent(String.valueOf(paymentMethod.getPercent().intValue()));
                paymentPercent.setLabel(paymentMethod.getPaymentMethod().getDisplayString());
                paymentPercentList.add(paymentPercent);
            }
        }
        else
        {
            // We are here because the user switched the selected project in the EditBlockDetails form
            // Get the first payment method associated with this project.
            PaymentMethod paymentMethod = paymentMethods.get(0);
            EditBlockDetailsForm.PaymentPercent paymentPercent = new EditBlockDetailsForm.PaymentPercent();
            paymentPercent.setPaymentMethodId(paymentMethod.getId());
            paymentPercent.setPaymentPercent("100");
            paymentPercent.setLabel(paymentMethod.getDisplayString());
            paymentPercentList.add(paymentPercent);
        }

        List<Researcher> instrumentOperators = Groups.getInstance().getMembers(Groups.INSTRUMENT_OPERATOR);
        List<Researcher> projectInstrumentOperators = project.getInstrumentOperators(instrumentOperators);

        EditBlockDetailsForm editForm = new EditBlockDetailsForm();
        SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a");
        editForm.setStartDate(dateFmt.format(firstBlock.getStartDate()));
        editForm.setEndDate(dateFmt.format(blocksToUpdate.get(blocksToUpdate.size() - 1).getEndDate()));
        editForm.setStartTime(timeFmt.format(firstBlock.getStartDate()));
        editForm.setEndTime(timeFmt.format(blocksToUpdate.get(blocksToUpdate.size() - 1).getEndDate()));
        editForm.setProjectId(projectId);
        editForm.setInstrumentId(instrumentId);
        editForm.setInstrumentName(instrument.getName());
        editForm.setUsageBlockIdsToEdit(usageBlockIdString);
        editForm.setPaymentPercentList(paymentPercentList);

        request.setAttribute("editBlockDetailsForm", editForm);

        // Get a list of ALL projects this user has access to.
        ProjectsSearcher projSearcher = new ProjectsSearcher();
        projSearcher.addType(new BilledProject().getShortType()); // billed projects
        Groups groupMan = Groups.getInstance();
        if(!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
            projSearcher.setResearcher(user.getResearcher());
        }
        List <Project> projects = projSearcher.search();
        request.setAttribute("projects", projects);



        return mapping.findForward("Success");
	}
}
