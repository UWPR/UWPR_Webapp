package org.uwpr.notice;

import org.uwpr.www.util.TimeUtils;

import java.util.Date;

public class Notice {

    private int id;
    private Date startDate;
    private Date endDate;
    private String message;
    private int createdBy;
    private Date createDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getStartDateString() {
        return startDate == null ? "" : TimeUtils.isoDate.format(startDate);
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getEndDateString() {
        return endDate == null ? "" : TimeUtils.isoDate.format(endDate);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreateDateString() {
        return createDate == null ? "" : TimeUtils.isoDate.format(createDate);
    }
}
