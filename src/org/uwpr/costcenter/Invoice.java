/**
 * Invoice.java
 * @author Vagisha Sharma
 * Jul 16, 2011
 */
package org.uwpr.costcenter;

import org.uwpr.www.util.TimeUtils;

import java.util.Date;

/**
 * 
 */
public class Invoice {

	private int id;
	private Date createDate;
	private Date billStartDate;
	private Date billEndDate;
	private int createdBy;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getBillStartDate() {
		return billStartDate;
	}
	public void setBillStartDate(Date billStartDate) {
		this.billStartDate = billStartDate;
	}
	public Date getBillEndDate() {
		return billEndDate;
	}
	public void setBillEndDate(Date billEndDate) {
		this.billEndDate = billEndDate;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public String toString()
	{
		StringBuilder str = new StringBuilder("ID: ").append(id);
		str.append("; ").append(TimeUtils.shortDate.format(billStartDate));
		str.append(" - ").append(TimeUtils.shortDate.format(billEndDate));
		return str.toString();
	}
}
