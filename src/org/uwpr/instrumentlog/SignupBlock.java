package org.uwpr.instrumentlog;

import org.uwpr.www.util.TimeUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by vsharma on 2/2/2017.
 */
public class SignupBlock
{
    private int id;
    private int instrumentSignupId;
    private int instrumentRateId;
    private Date startDate;
    private Date endDate;
    //private List<SignupPayment> payments;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getInstrumentSignupId()
    {
        return instrumentSignupId;
    }

    public void setInstrumentSignupId(int instrumentSignupId)
    {
        this.instrumentSignupId = instrumentSignupId;
    }

    public int getInstrumentRateId()
    {
        return instrumentRateId;
    }

    public void setInstrumentRateId(int instrumentRateId)
    {
        this.instrumentRateId = instrumentRateId;
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

//    public List<SignupPayment> getPayments()
//    {
//        return payments == null ? Collections.<SignupPayment>emptyList() : payments;
//    }
//
//    public void setPayments(List<SignupPayment> payments)
//    {
//        this.payments = payments;
//    }

    public int getHours()
    {
        long time = getEndDate().getTime() - getStartDate().getTime();
        return (int) (time / TimeUtils.MILLIS_IN_HOUR);
    }
}
