/**
 * UsageBlockPaymentInformation.java
 * @author Vagisha Sharma
 * Jul 15, 2011
 */
package org.uwpr.scheduler;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.uwpr.www.util.TimeUtils;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.project.payment.PaymentMethodDAO;
import org.yeastrc.project.payment.ProjectPaymentMethodDAO;

import javax.print.attribute.standard.DateTimeAtCompleted;

/**
 * 
 */
public class UsageBlockPaymentInformation {

	private List<PaymentMethodAndPercent> paymentMethodList;
	private final List<PaymentMethod> projectPaymentMethodList;
	
	public UsageBlockPaymentInformation(int projectId) throws SchedulerException {
		// get a list of payment methods for this project
		try {
			projectPaymentMethodList = ProjectPaymentMethodDAO.getInstance().getCurrentPaymentMethods(projectId);
		} catch (SQLException e) {
			throw new SchedulerException("Error getting payment methods for project ID "+projectId, e);
		}
		
		paymentMethodList = new ArrayList<PaymentMethodAndPercent>();
	}
	
	public void add(String paymentMethodIdString, String percentString, Date endDate) throws SchedulerException {
		
		if(paymentMethodIdString == null)
			return;
		
		int paymentMethodId = 0;
		try {
        	paymentMethodId = Integer.parseInt(paymentMethodIdString);
        }
        catch(NumberFormatException e) {
        	throw new SchedulerException("Error parsing payment method ID:  "+paymentMethodIdString);
        }
        if(paymentMethodId == 0) {
        	//return;
        	throw new SchedulerException("Invalid payment method ID: "+paymentMethodIdString);
        }
        	
        
        // make sure the payment method is associated with the project
        PaymentMethod paymentMethod = null;
        for(PaymentMethod pm: projectPaymentMethodList) {
        	if(pm.getId() == paymentMethodId) {
        		paymentMethod = pm;
        		break;
        	}
        }
        if(paymentMethod == null) {
        	PaymentMethod method;
			try {
				method = PaymentMethodDAO.getInstance().getPaymentMethod(paymentMethodId);
			} catch (SQLException e) {
				throw new SchedulerException("Error getting details for payment method ID "+paymentMethodId);
			}
        	throw new SchedulerException("Payment method selected: "+method.getDisplayString()+" is not associated with the project");
        }

		// If this payment method has an expiration date make sure it does not expire before the given endDate
		if(paymentMethod.getBudgetExpirationDate() != null)
		{
			try
			{
				Date endDateOnly = TimeUtils.shortDate.parse(TimeUtils.shortDate.format(endDate)); // Get just the date component
				if(endDateOnly.after(paymentMethod.getBudgetExpirationDate()))
				{
					throw new SchedulerException(paymentMethod.getShortDisplayString() + " expires on " +
							TimeUtils.shortDate.format(paymentMethod.getBudgetExpirationDate()) + ". Please select a different payment method or adjust the end date (" +
							TimeUtils.shortDate.format(endDate) + ") of the requested time. ");
				}
			} catch (ParseException e)
			{
				throw new SchedulerException("Error parsing end date " + endDate);
			}
		}

        
        BigDecimal paymentMethodPerc = null; // percent to be billed to this payment method
        try {
        	paymentMethodPerc = new BigDecimal(percentString);
        }
        catch(NumberFormatException e) {
        	throw new SchedulerException("Error parsing percent billed to payment method: "+percentString);
        }
        
        
        // If this payment method is already part of the list throw an exception
        for(PaymentMethodAndPercent method: this.paymentMethodList) {
        	if(method.getPaymentMethod().getId() == paymentMethodId) {
        		throw new SchedulerException("A payment method cannot be selected more than once");
        	}
        }
        
        PaymentMethodAndPercent methodAndPerc = new PaymentMethodAndPercent(paymentMethod, paymentMethodPerc);
        this.paymentMethodList.add(methodAndPerc);
        
        // Total percent billed to each payment method must not exceed 100%
        BigDecimal total = BigDecimal.ZERO;
        for(PaymentMethodAndPercent mp: this.paymentMethodList) {
        	total.add(mp.percent);
        	if(total.compareTo(new BigDecimal("100.0")) == 1) {
        		throw new SchedulerException("Total percent billed to individual payment methods cannot exceed 100%");
        	}
        }
	}
	
	public PaymentMethod getPaymentMethod(int index) {
		
		return this.paymentMethodList.get(index).getPaymentMethod();
	}
	
	public BigDecimal getPercent(int index) {
		
		return this.paymentMethodList.get(index).getPercent();
	}
	
	public int getCount() {
		return this.paymentMethodList.size();
	}
	
	public static final class PaymentMethodAndPercent {
		
		private final PaymentMethod paymentMethod;
		private final BigDecimal percent;
		
		public PaymentMethodAndPercent(PaymentMethod paymentMethod, BigDecimal percent) {
			
			this.paymentMethod = paymentMethod;
			this.percent = percent;
		}

		public PaymentMethod getPaymentMethod() {
			return paymentMethod;
		}

		public BigDecimal getPercent() {
			return percent;
		}
	}
}
