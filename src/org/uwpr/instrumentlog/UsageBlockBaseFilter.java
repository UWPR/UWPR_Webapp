package org.uwpr.instrumentlog;

import java.util.Date;

/**
 * Created by vsharma on 1/29/2016.
 */
public class UsageBlockBaseFilter
{
    private int projectId;
    private int instrumentId;
    private int instrumentOperatorId;
    private int instrumentRateId;
    private Date startDate;
    private Date endDate;
    private boolean containedInRange = true;
    private boolean trimToFit;
    private String sortColumn;

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

    public int getInstrumentOperatorId()
    {
        return instrumentOperatorId;
    }

    public void setInstrumentOperatorId(int instrumentOperatorId)
    {
        this.instrumentOperatorId = instrumentOperatorId;
    }

    public int getInstrumentId()
    {
        return instrumentId;
    }

    public void setInstrumentId(int instrumentId)
    {
        this.instrumentId = instrumentId;
    }

    public int getInstrumentRateId()
    {
        return instrumentRateId;
    }

    public void setInstrumentRateId(int instrumentRateId)
    {
        this.instrumentRateId = instrumentRateId;
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

    public boolean isTrimToFit()
    {
        return trimToFit;
    }

    public void setTrimToFit(boolean trimToFit)
    {
        this.trimToFit = trimToFit;
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
