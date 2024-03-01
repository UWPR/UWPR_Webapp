/**
 * 
 */
package org.yeastrc.project.payment;

import org.uwpr.costcenter.Cost;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;

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

    public BigDecimal getTotalCost()
	{
		return cost == null ? BigDecimal.ZERO : cost.getTotal();
	}

	public BigDecimal getInstrumentCost()
	{
		return cost == null ? BigDecimal.ZERO : cost.instrumentCost;
	}

	public BigDecimal getSetupCost()
	{
		return cost == null ? BigDecimal.ZERO : cost.setupCost;
	}

	public BigDecimal getInvoicedCost()
	{
		return invoicedCost == null ? BigDecimal.ZERO : invoicedCost.getTotal();
	}

	public String getTotalCostFormatted()
	{
		return format(getTotalCost());
	}

	public String getInstrumentCostFormatted()
	{
		return format(getInstrumentCost());
	}

	public String getSetupCostFormatted()
	{
		return format(getSetupCost());
	}

	public String getInvoicedCostFormatted()
	{
		return format(getInvoicedCost());
	}

	private String format(BigDecimal cost)
	{
		return NumberFormat.getCurrencyInstance().format(cost);
	}
}
