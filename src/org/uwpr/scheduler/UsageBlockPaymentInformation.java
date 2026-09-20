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

/**
 * 
 */
public class UsageBlockPaymentInformation {

	private List<PaymentMethodAndPercent> paymentMethodList;
	private final List<PaymentMethod> projectPaymentMethodList;
	
	public UsageBlockPaymentInformation(int projectId) throws SchedulerException {
		this(getCurrentPaymentMethods(projectId));
	}

	/**
	 * @param projectPaymentMethodList the project's current payment methods, the only ones add() accepts
	 */
	UsageBlockPaymentInformation(List<PaymentMethod> projectPaymentMethodList) {
		this.projectPaymentMethodList = projectPaymentMethodList;
		paymentMethodList = new ArrayList<PaymentMethodAndPercent>();
	}

	private static List<PaymentMethod> getCurrentPaymentMethods(int projectId) throws SchedulerException {
		try {
			return ProjectPaymentMethodDAO.getInstance().getCurrentPaymentMethods(projectId);
		} catch (SQLException e) {
			throw new SchedulerException("Error getting payment methods for project ID "+projectId, e);
		}
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
			if(method == null) {
				throw new SchedulerException("No payment method found for ID "+paymentMethodId);
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

		// Checked before the percent is stored, so every percent in paymentMethodList is a whole number
		// above 0.  The getters can be read without calling checkPercents().
		if(paymentMethodPerc.signum() <= 0) {
			throw new SchedulerException("Percent billed to a payment method must be greater than 0. Found "
					+ paymentMethodPerc.toPlainString() + "% for " + paymentMethod.getShortDisplayString());
		}
		if(paymentMethodPerc.remainder(BigDecimal.ONE).signum() != 0) {
			throw new SchedulerException("Percent billed to a payment method must be a whole number. Found "
					+ paymentMethodPerc.toPlainString() + "% for " + paymentMethod.getShortDisplayString());
		}


        // If this payment method is already part of the list throw an exception
        for(PaymentMethodAndPercent method: this.paymentMethodList) {
        	if(method.getPaymentMethod().getId() == paymentMethodId) {
        		throw new SchedulerException("A payment method cannot be selected more than once");
        	}
        }
        
        // Checked before the payment method is added, so a refused one is left out of the list.  This is
        // not checkPercents(), which refuses a finished list that does not total exactly 100.
        BigDecimal total = BigDecimal.ZERO;
        for(PaymentMethodAndPercent mp: this.paymentMethodList) {
        	total = total.add(mp.percent);
        }
        total = total.add(paymentMethodPerc);
        if(total.compareTo(new BigDecimal("100")) > 0) {
        	throw new SchedulerException("Total percent billed to individual payment methods cannot exceed 100%");
        }

        this.paymentMethodList.add(new PaymentMethodAndPercent(paymentMethod, paymentMethodPerc));
	}
	
	/**
	 * Throws a SchedulerException unless the percents added total 100.  add() has already refused any
	 * percent that is not a whole number above 0.
	 */
	public void checkPercents() throws SchedulerException {

		BigDecimal total = BigDecimal.ZERO;
		for(PaymentMethodAndPercent mp: this.paymentMethodList) {
			total = total.add(mp.percent);
		}
		if(total.compareTo(new BigDecimal("100")) != 0) {
			throw new SchedulerException("Total percent billed to individual payment methods must be 100%. Found "
					+ total.toPlainString() + "%");
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
