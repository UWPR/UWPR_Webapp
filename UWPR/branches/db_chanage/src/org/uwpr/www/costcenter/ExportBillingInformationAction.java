/**
 * ExportBillingInformationAction.java
 * @author Vagisha Sharma
 * Jun 18, 2011
 */
package org.uwpr.www.costcenter;

import org.apache.log4j.Logger;
import org.apache.struts.action.*;
import org.uwpr.costcenter.*;
import org.uwpr.www.util.TimeUtils;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 */
public class ExportBillingInformationAction extends Action {

	private static final Logger log = Logger.getLogger(ExportBillingInformationAction.class);
	
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM.dd.yyyy");
	
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
		
		// Restrict access to administrators
        Groups groupMan = Groups.getInstance();
        if (!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
            ActionErrors errors = new ActionErrors();
            errors.add("access", new ActionMessage("error.access.invalidgroup"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        ExportBillingInformationForm exportForm = (ExportBillingInformationForm) form;
        
        java.util.Date startDate = exportForm.getStartDate();
        java.util.Date endDate = exportForm.getEndDate();
        int projectId = exportForm.getProjectId();
        
       
        //PrintWriter writer = response.getWriter();
        ServletOutputStream outStream = response.getOutputStream();
        
        // response.setContentType("text/plain");
        response.setContentType("application/vnd.ms-excel");
    	response.setHeader("Content-Disposition","attachment; filename=\"Billing_"+dateFormatter.format(startDate)+"_TO_"+dateFormatter.format(endDate)+".xls\"");
    	response.setHeader("cache-control", "no-cache");

    	Invoice invoice = null;
        try {
        	
        	BillingInformationExcelExporter exporter = new BillingInformationExcelExporter();

			Date startBillDate = TimeUtils.makeBeginningOfDay(startDate);
			Date endBillDate = TimeUtils.makeEndOfDay(endDate);
        	exporter.setStartDate(startBillDate);
			exporter.setEndDate(endBillDate);
        	
        	exporter.setSummarize(exportForm.isSummarize());
        	
        	// Are we exporting an invoice -- blocks included in the invoice will be 
        	// marked.
        	if(exportForm.isExportInvoice()) {
        		
        		invoice = InvoiceDAO.getInstance().getInvoice(startBillDate, endBillDate);
            	if(invoice == null) {
            		invoice = new Invoice();
            		invoice.setBillStartDate(startBillDate);
            		invoice.setBillEndDate(endBillDate);
            		invoice.setCreatedBy(user.getResearcher().getID());
            		InvoiceDAO.getInstance().save(invoice);
            	}
            	
            	InvoiceBlockCreator invoiceBlockCreator = new InvoiceBlockCreator(invoice);
            	exporter.setBillinInformationExporterListener(invoiceBlockCreator);
        	}
        	
        	if(projectId != 0)
        		//exporter.export(projectId, writer);
        		exporter.exportToXls(projectId, outStream);
        	else
        		//exporter.export(writer);
        		exporter.exportToXls(outStream);
        	
        	//throw new Exception("testing exception");
        }
        catch(BillingInformationExporterException e) {
        	log.error("Error exporting data", e);
        	
        	if(invoice != null) {
        		log.info("Deleting invoice ID: "+invoice.getId());
        		InvoiceDAO.getInstance().delete(invoice);
        	}
        	response.reset();
        	
        	ActionErrors errors = new ActionErrors();
            errors.add("costcenter", new ActionMessage("error.costcenter.export", e.getMessage()));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        catch(Exception e) {
        	log.error("Exception writing response", e);
        	
        	if(invoice != null) {
        		log.info("Deleting invoice ID: "+invoice.getId());
        		InvoiceDAO.getInstance().delete(invoice);
        	}
        	
        	response.reset();
        	//writer.close();  // do not call this it will commit the response
        	//if(response.isCommitted()) {
        	//	log.info("committed");
        	//}
        	
        	ActionErrors errors = new ActionErrors();
            errors.add("costcenter", new ActionMessage("error.costcenter.export", e.getMessage()));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        return null;
	}
}
