package org.uwpr.instrumentlog;

import java.util.Date;

/**
 * Created by vsharma on 1/9/2017.
 */
public class SignupFilter
{
    private int projectId;
    private int instrumentId;
    private Date startDate;
    private Date endDate;
    private boolean containedInRange = true;
    private String sortColumn;
    private int paymentMethodId;

    public int getPaymentMethodId()
    {
        return paymentMethodId;
    }

    public void setPaymentMethodId(int paymentMethodId)
    {
        this.paymentMethodId = paymentMethodId;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public int getInstrumentId()
    {
        return instrumentId;
    }

    public void setInstrumentId(int instrumentId)
    {
        this.instrumentId = instrumentId;
    }

    public int getProjectId()
    {
        return projectId;
    }

    public void setProjectId(int projectId)
    {
        this.projectId = projectId;
    }

    public boolean isContainedInRange()
    {
        return containedInRange;
    }

    public void setContainedInRange(boolean containedInRange)
    {
        this.containedInRange = containedInRange;
    }

    public String getSortColumn()
    {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn)
    {
        this.sortColumn = sortColumn;
    }
}
