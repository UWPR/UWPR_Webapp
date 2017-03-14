/**
 * 
 */
package org.uwpr.instrumentlog;

import java.math.BigDecimal;

import org.yeastrc.project.payment.PaymentMethod;

/**
 * InstrumentUsagePayment.java
 * @author Vagisha Sharma
 * Jun 2, 2011
 * 
 */
public class InstrumentUsagePayment extends Payment{

	private int instrumentUsageId;

	public int getInstrumentUsageId() {
		return instrumentUsageId;
	}
	
	public void setInstrumentUsageId(int instrumentUsageId) {
		this.instrumentUsageId = instrumentUsageId;
	}
}
