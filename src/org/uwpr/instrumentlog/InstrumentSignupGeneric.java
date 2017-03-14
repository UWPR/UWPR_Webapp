package org.uwpr.instrumentlog;

import org.uwpr.www.util.TimeUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by vsharma on 2/2/2017.
 */
public class InstrumentSignupGeneric<T extends SignupBlock> implements Block
{
    private int id;
    private int projectID;
    private int instrumentID;
    private Date startDate;
    private Date endDate;
    private int createdBy; // researcherId: who created this usage block
    private Date dateCreated;

    private List<T> blocks;

    private List<SignupPayment> payments;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getProjectID()
    {
        return projectID;
    }

    public void setProjectId(int projectId)
    {
        this.projectID = projectId;
    }

    public int getInstrumentID()
    {
        return instrumentID;
    }

    public void setInstrumentID(int instrumentID)
    {
        this.instrumentID = instrumentID;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public int getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(int createdBy)
    {
        this.createdBy = createdBy;
    }

    public Date getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(Date created)
    {
        this.dateCreated = created;
    }

    public List<T> getBlocks()
    {
        return blocks == null ? Collections.<T>emptyList() : blocks;
    }

    public void setBlocks(List<T> blocks)
    {
        this.blocks = blocks;
    }

    public List<SignupPayment> getPayments()
    {
        return payments;
    }

    public void setPayments(List<SignupPayment> payments)
    {
        this.payments = payments;
    }

    public int getHours()
    {
        long time = getEndDate().getTime() - getStartDate().getTime();
        return (int) (time / TimeUtils.MILLIS_IN_HOUR);
    }

    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("Id: " + getId());
        buf.append(", ");
        buf.append("Project: ").append(getProjectID());
        buf.append(", ");
        buf.append("Instrument: ").append(getInstrumentID());
        buf.append(", ");
        buf.append("Start: ").append(getStartDate());
        buf.append(", ");
        buf.append("End: ").append(getEndDate());
        buf.append(", ");
        buf.append("Created: ").append(getDateCreated());
        buf.append(", ");
        buf.append("CreatedBy: ").append(getCreatedBy());
        return buf.toString();
    }
}
