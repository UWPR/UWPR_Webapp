package org.uwpr.instrumentlog;

import org.uwpr.scheduler.UsageBlockBaseWithRate;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class UsageBlock extends UsageBlockBaseWithRate {

	private String piName;
	private int piID;
	private String instrumentName;
	private String operatorName;
	private String projectTitle;
	private List<InstrumentUsagePayment> payments;
	private Date invoiceDate;
	
	public UsageBlock() {
	    super();
	}
	
	public String getProjectPI() {
		return this.piName;
	}
	
	public void setProjectPI(String piName) {
	    this.piName = piName;
	}

	public String getOperatorName()
	{
		return operatorName;
	}

	public void setOperatorName(String operatorName)
	{
		this.operatorName = operatorName;
	}

	public int getPIID() {
		return piID;
	}
	
	public void setPIID(int piID) {
	    this.piID = piID;
	}
	
	public List<InstrumentUsagePayment> getPayments()
	{
		return payments == null ? Collections.<InstrumentUsagePayment>emptyList() : payments;
	}

	public void setPayments(List<InstrumentUsagePayment> payments) {
		this.payments = payments;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
        buf.append("UsageBlock:\n");
        buf.append("projectId: "+getProjectID()+" "+getProjectTitle()+"\n");
        buf.append("piId: "+getPIID()+" "+getProjectPI()+"\n");
        buf.append("; instrumentId: "+getInstrumentID()+" "+getInstrumentName()+"\n");
        //buf.append("; paymentMethodId: "+getPaymentMethodID()+" "+getPaymentMethodName()+"\n");
        if(payments != null) {
        	for(InstrumentUsagePayment payment: payments) {
        		buf.append("Payment ID: "+payment.getPaymentMethod().getId()+
        				   "; Name: "+payment.getPaymentMethod().getDisplayString()+
        				   "; Percent: "+payment.getPercent()+"\n");
        	}
        }
        else {
        	buf.append("No payments associated with usage\n");
        }
        buf.append("; "+getStartDate().toString()+" - "+getEndDate().toString());
        return buf.toString();
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	public String getProjectTitle() {
		return projectTitle;
	}

	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}
	
	public boolean isBilled() {
		return invoiceDate != null;
	}

	public Date getInvoiceDate() {
		return invoiceDate;
	}
	
	public String getInvoiceDateFormatted() {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return df.format(getInvoiceDate());
	}

	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	
	public UsageBlock newBlock() {
        return new UsageBlock();
    }
}
