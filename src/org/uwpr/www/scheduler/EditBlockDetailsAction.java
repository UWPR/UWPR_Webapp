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
import org.uwpr.scheduler.PatternToDateConverter;
import org.uwpr.scheduler.SchedulerException;
import org.uwpr.scheduler.UsageBlockPaymentInformation;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.project.payment.PaymentMethodDAO;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * 
 */
public class EditBlockDetailsAction extends Action {

	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			return returnError(mapping, request, "username", 
					new ActionMessage("error.login.notloggedin"), "authenticate");
		}
		
		// Get the form
        EditBlockDetailsForm editForm = (EditBlockDetailsForm) form;
		
		// we need a projectID
        int projectId = editForm.getProjectId();
        
		
        // Make sure the user has access to the project
        Project project = null;
        try {
        	project = ProjectFactory.getProject(projectId);
        	
        	if(project == null) {
        		return returnError(mapping, request, "scheduler", 
        				new ActionMessage("error.scheduler.invalidid", "Project with ID: "+projectId+" not found in the database."),
        				"standardHome");
        	}
        }
        catch(Exception e) {
        	return returnError(mapping, request, "scheduler", 
        			new ActionMessage("error.costcenter.load", e.getMessage()),
    				"standardHome");
        }
        try {
        	
        	if(!project.checkAccess(user.getResearcher())) {
        		return returnError(mapping, request, "scheduler", 
        				new ActionMessage("error.costcenter.invalidaccess", 
                        		"User does not have access to edit instrument time for project "+projectId+"."),
        				"viewProject", "?ID="+projectId);
        	}
        }
        catch(Exception e) {
        	return returnError(mapping, request, "scheduler", 
        			new ActionMessage("error.costcenter.load", 
                    		"Project ID: "+projectId+". ERROR: "+e.getMessage()),
    				"viewProject", "?ID="+projectId);
        }
        

        Calendar startCal = Calendar.getInstance();
        try
        {
            Date startDate = PatternToDateConverter.convert(editForm.getStartDate(), "10");
            if(startDate != null)
            {
                startCal.setTime(startDate);
                request.getSession().setAttribute("scheduler_year", startCal.get(Calendar.YEAR));
                request.getSession().setAttribute("scheduler_month", startCal.get(Calendar.MONTH));
            }
        }
        catch(Exception ignored){}


        int instrumentId = editForm.getInstrumentId();


		// get the usageBlockIds from the request
        List<Integer> usageBlockIds = new ArrayList<Integer>();
        String usageBlockIdString = editForm.getUsageBlockIdsToEdit();
        if(usageBlockIdString != null) {
        	String[] tokens = usageBlockIdString.split(",");
            for(String token: tokens) {

            	try {
            		usageBlockIds.add(Integer.parseInt(token));
            	}
            	catch(NumberFormatException e) {
            		return returnError(mapping, request, "scheduler", 
            				new ActionMessage("error.costcenter.invaliddata", 
                            		"Invalid usage block ID found in request: "+token+"."),
            				"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
            	}
            }
        }
        if(usageBlockIds.size() == 0) {
        	return returnError(mapping, request, "scheduler", 
        			new ActionMessage("error.costcenter.invaliddata", 
            		"No usage block IDs found in request."),
    				"standardHome");
        }
        
        
        // load the usage blocks
        List<UsageBlockBase> blocksToUpdate = new ArrayList<UsageBlockBase>(usageBlockIds.size());
        Date rangeEndDate = null;
        for(int usageBlockId: usageBlockIds) {
        	
        	UsageBlockBase usageBlock = UsageBlockBaseDAO.getUsageBlockBase(usageBlockId);
        	if(usageBlock == null) {
        		return returnError(mapping, request, "scheduler", 
        				new ActionMessage("error.costcenter.invaliddata", 
        						"No usage block found for usageBlockId: "+usageBlockId),
        						"viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
        	}

            // If this block has already been billed throw an error
            InvoiceInstrumentUsage billedBlock = InvoiceInstrumentUsageDAO.getInstance().getInvoiceBlock(usageBlock.getID());
            if(billedBlock != null) {
                return returnError(mapping, request, "scheduler",
                        new ActionMessage("error.costcenter.invalidaccess",
                                "Usage block : "+usageBlockId +" has already been billed."),
                                "viewScheduler", "?projectId="+projectId+"&instrumentId="+instrumentId);
            }

            blocksToUpdate.add(usageBlock);
            Date blkEndDate = usageBlock.getEndDate();
            rangeEndDate = rangeEndDate == null ? blkEndDate : (blkEndDate.after(rangeEndDate) ? blkEndDate : rangeEndDate);
        }

        // Get the selected payment methods
        InstrumentUsagePaymentDAO paymentDao = InstrumentUsagePaymentDAO.getInstance();
        PaymentMethodDAO paymentMethodDao = PaymentMethodDAO.getInstance();
        List<EditBlockDetailsForm.PaymentPercent> paymentPercentList = editForm.getPaymentPercentList();
        Map<Integer, PaymentMethod> paymentMethodMap = new HashMap<Integer, PaymentMethod>();

        // Load the payment methods
        for(EditBlockDetailsForm.PaymentPercent paymentPercent: paymentPercentList)
        {
            if(paymentPercent.getPaymentPercentInteger() == 0.0)
                continue;

            PaymentMethod paymentMethod = paymentMethodDao.getPaymentMethod(paymentPercent.getPaymentMethodId());
            paymentMethodMap.put(paymentPercent.getPaymentMethodId(), paymentMethod);
        }

        // If the payment method(s) is expiring before the end data, throw an error.
        UsageBlockPaymentInformation paymentInfo = new UsageBlockPaymentInformation(projectId);
        for(Integer paymentMethodId: paymentMethodMap.keySet()) {
            try
            {
                paymentInfo.add(String.valueOf(paymentMethodId), String.valueOf(paymentMethodId), rangeEndDate);
            }
            catch(SchedulerException e)
            {
                return returnError(mapping, request, "scheduler",
                        new ActionMessage("error.costcenter.invaliddata", e.getMessage()),
                        "viewEditBlockDetailsForm", "?projectId="+projectId+"&instrumentId="+instrumentId+"&usageBlockIds="+usageBlockIdString);
            }
        }

        Connection conn = null;
        try {
            conn = DBConnectionManager.getMainDbConnection();
            conn.setAutoCommit(false);

            InstrumentLogDao logDao = InstrumentLogDao.getInstance();

            // Delete the old payment methods and add new ones
            for (UsageBlockBase block : blocksToUpdate) {
                paymentDao.deletePaymentsForUsage(conn, block.getID());

                block.setUpdaterResearcherID(user.getResearcher().getID());

                for (EditBlockDetailsForm.PaymentPercent paymentPercent : paymentPercentList) {
                    if (paymentPercent.getPaymentPercentInteger() == 0.0)
                        continue;

                    InstrumentUsagePayment iup = new InstrumentUsagePayment();
                    iup.setInstrumentUsageId(block.getID());
                    PaymentMethod paymentMethod = paymentMethodMap.get(paymentPercent.getPaymentMethodId());
                    iup.setPaymentMethod(paymentMethod);
                    iup.setPercent(BigDecimal.valueOf(paymentPercent.getPaymentPercentInteger()));
                    paymentDao.savePayment(conn, iup);

                    logDao.logSignupEdited(conn, block, block.getUpdaterResearcherID(),
                            "Changed payment method for block to (" + iup.getPercent() + "%) " + paymentMethod.getDisplayString());
                }
            }

            // If the project associated with the blocks have changed, update the blocks in the database
            List<UsageBlockBase> changedBlocks = new ArrayList<UsageBlockBase>();
            for (UsageBlockBase blk : blocksToUpdate) {
                if (blk.getProjectID() != projectId) {
                    changedBlocks.add(blk);
                }
            }
            InstrumentUsageDAO instrumentUsageDAO = InstrumentUsageDAO.getInstance();
            instrumentUsageDAO.updateBlocksProject(conn, changedBlocks, projectId);

            conn.commit();
        }
        catch(Exception e)
        {
            return returnError(mapping, request, "scheduler",
                    new ActionMessage("error.costcenter.invaliddata", "There was an error saving changes to usage blocks. Error: " + e.getMessage()),
                    "viewScheduler", "?projectId=" + projectId + "&instrumentId=" + instrumentId);
        }
        finally
        {
            if(conn != null) {try {conn.close();} catch(SQLException ignored){}};
        }

        ActionForward fwd = mapping.findForward("viewScheduler");
        return new ActionForward(fwd.getPath()+"?projectId="+projectId+"&instrumentId="+instrumentId, true);
	}

	private ActionForward returnError(ActionMapping mapping,
			HttpServletRequest request, String errProperty, ActionMessage message, String forward) {
		ActionErrors errors = new ActionErrors();
		errors.add(errProperty, message);
		saveErrors( request, errors );
		return mapping.findForward(forward);
	}
	
	private ActionForward returnError(ActionMapping mapping,
			HttpServletRequest request, String errProperty, ActionMessage message, 
			String forward, String appendToFwdPath) {
		ActionErrors errors = new ActionErrors();
		errors.add(errProperty, message);
		saveErrors( request, errors );
		ActionForward fwd = mapping.findForward(forward);
        return new ActionForward(fwd.getPath()+appendToFwdPath, fwd.getRedirect());
	}
}
