package org.uwpr.instrumentlog;

/**
 * Created by vsharma on 1/29/2016.
 */
public class UsageBlockFilter extends UsageBlockBaseFilter
{
    private int paymentMethodId;

    public int getPaymentMethodId()
    {
        return paymentMethodId;
    }

    public void setPaymentMethodId(int paymentMethodId)
    {
        this.paymentMethodId = paymentMethodId;
    }
}
