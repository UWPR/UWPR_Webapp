/**
 * 
 */
package org.uwpr.instrumentlog;

import org.yeastrc.project.payment.PaymentMethod;

import java.math.BigDecimal;

/**
 * Payment.java
 * @author Vagisha Sharma
 * 
 */
public class Payment
{
	private PaymentMethod paymentMethod;
	private BigDecimal percent;

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public BigDecimal getPercent() {
		return percent;
	}
	
	public void setPercent(BigDecimal percent) {
		this.percent = percent;
	}
}
