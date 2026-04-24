package org.uwpr.www.notice;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.www.util.TimeUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Date;

public class NoticeForm extends ActionForm {

    private String idString;
    private String startDateString;
    private String endDateString;
    private String message;

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        Date sdate = null;
        try {
            sdate = getStartDate();
        }
        catch (ParseException e) {
            errors.add("notice", new ActionMessage("error.costcenter.invaliddata", "Invalid start date: " + startDateString));
        }

        Date edate = null;
        try {
            edate = getEndDate();
        }
        catch (ParseException e) {
            errors.add("notice", new ActionMessage("error.costcenter.invaliddata", "Invalid end date: " + endDateString));
        }

        if (sdate != null && edate != null && edate.before(sdate)) {
            errors.add("notice", new ActionMessage("error.costcenter.invaliddata", "End date cannot be before start date."));
        }

        if (message == null || message.trim().length() == 0) {
            errors.add("notice", new ActionMessage("error.costcenter.invaliddata", "Message cannot be empty."));
        }

        return errors;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        // ActionForm instances are reused; clear fields between requests.
        idString = null;
        startDateString = null;
        endDateString = null;
        message = null;
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }

    public int getId() {
        try {
            return Integer.parseInt(idString);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setId(int id) {
        this.idString = String.valueOf(id);
    }

    public String getStartDateString() {
        return startDateString;
    }

    public void setStartDateString(String startDateString) {
        this.startDateString = startDateString;
    }

    public Date getStartDate() throws ParseException {
        return TimeUtils.shortDate.parse(startDateString);
    }

    public void setStartDate(Date startDate) {
        this.startDateString = TimeUtils.shortDate.format(startDate);
    }

    public String getEndDateString() {
        return endDateString;
    }

    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
    }

    public Date getEndDate() throws ParseException {
        return TimeUtils.shortDate.parse(endDateString);
    }

    public void setEndDate(Date endDate) {
        this.endDateString = TimeUtils.shortDate.format(endDate);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
