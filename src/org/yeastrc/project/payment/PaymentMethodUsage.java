/**
 * 
 */
package org.yeastrc.project.payment;

import org.uwpr.costcenter.Cost;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * PaymentMethod.java
 * @author Vagisha Sharma
 * Jun 29, 2018
 * 
 */
public class PaymentMethodUsage
{
    private Cost invoicedCost;
	private Cost cost;

	public PaymentMethodUsage(int paymentMethodId) throws SQLException
	{
		cost = PaymentMethodDAO.getInstance().getCost(paymentMethodId);
		invoicedCost = PaymentMethodDAO.getInstance().getInvoicedCost(paymentMethodId);
	}

    public BigDecimal getTotalCost() {
		return cost == null ? BigDecimal.ZERO : cost.getTotal();
    }

	public BigDecimal getInstrumentCost()
	{
		return cost == null ? BigDecimal.ZERO : cost.instrumentCost;
	}

	public BigDecimal getSignupCost()
	{
		return cost == null ? BigDecimal.ZERO : cost.signupCost;
	}

	public BigDecimal getSetupCost()
	{
		return cost == null ? BigDecimal.ZERO : cost.setupCost;
	}

    public BigDecimal getInvoicedCost() {
        return invoicedCost == null ? BigDecimal.ZERO : invoicedCost.getTotal();
    }
}
