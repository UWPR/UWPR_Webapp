package org.uwpr.www.scheduler;

import org.apache.struts.action.ActionForm;
import org.uwpr.instrumentlog.UsageBlockFilter;
import org.uwpr.www.util.TimeUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: vsharma
 * Date: 9/6/13
 * Time: 10:51 PM
 */
public class TimeScheduledFilterForm extends ActionForm {

    private int projectId = 0;
    private int instrumentId = 0;
    private int instrumentOperatorId = 0;
    private int paymentMethodId = 0;
    private String startDateString;
    private String endDateString;;

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(int instrumentId) {
        this.instrumentId = instrumentId;
    }

    public int getInstrumentOperatorId()
    {
        return instrumentOperatorId;
    }

    public void setInstrumentOperatorId(int instrumentOperatorId)
    {
        this.instrumentOperatorId = instrumentOperatorId;
    }

    public int getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(int paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getStartDateString() {
        return startDateString;
    }

    public void setStartDateString(String startDateString) {
        this.startDateString = startDateString;
    }

    public String getEndDateString() {
        return endDateString;
    }

    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
    }

    public void setFilterCriteria(UsageBlockFilter filter)
    {
        setProjectId(filter.getProjectId());
        setInstrumentId(filter.getInstrumentId());
        setInstrumentOperatorId(filter.getInstrumentOperatorId());
        setPaymentMethodId(filter.getPaymentMethodId());
        setStartDateString(filter.getStartDate() == null ? "" : TimeUtils.shortDate.format(filter.getStartDate()));
        setEndDateString(filter.getEndDate() == null ? "" : TimeUtils.shortDate.format(filter.getEndDate()));
    }
}
