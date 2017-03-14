package org.uwpr.www.scheduler;

import org.apache.struts.action.ActionForm;
import org.uwpr.instrumentlog.SignupFilter;
import org.uwpr.instrumentlog.UsageBlockFilter;

import java.text.SimpleDateFormat;

/**
 * User: vsharma
 * Date: 2/6/17
 * Time: 10:51 PM
 */
public class SignupFilterFilterForm extends ActionForm {

    private int projectId = 0;
    private int instrumentId = 0;
    private int paymentMethodId = 0;
    private String startDateString;
    private String endDateString;;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

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

    public void setFilterCriteria(SignupFilter filter)
    {
        setProjectId(filter.getProjectId());
        setInstrumentId(filter.getInstrumentId());
        setPaymentMethodId(filter.getPaymentMethodId());
        setStartDateString(filter.getStartDate() == null ? "" : dateFormat.format(filter.getStartDate()));
        setEndDateString(filter.getEndDate() == null ? "" : dateFormat.format(filter.getEndDate()));
    }
}
