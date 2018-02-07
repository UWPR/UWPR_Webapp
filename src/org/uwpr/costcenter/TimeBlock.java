/**
 * TimeBlock.java
 * @author Vagisha Sharma
 * Apr 29, 2011
 */
package org.uwpr.costcenter;

import org.uwpr.www.util.TimeUtils;

import java.util.Date;

/**
 * Represents a block of time that a user can reserve an instrument for.
 * e.g. 4 hour blocks: 9am to 1pm; 1pm to 5pm
 *      8 hour blocks: 9am to 5pm
 *     16 hour blocks: 5pm to 9am  (next day)
 *     24 hour blocks: 9am to 9am  (next day) 
 */
public class TimeBlock {

	private int id;
	private Date startTime;
	private int numHours;
	private String name;
	private Date createDate;

	public static final String HOURLY = "hourly";
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public String getStartTimeString() {
		if(startTime == null) 
			return "-";
		else return TimeUtils.timeFormat.format(startTime.getTime());
	}
	
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public Date getEndTime() {
		if(startTime == null)
			return null;
		else {
			Date endTime = new Date(startTime.getTime() + getNumHoursAsMilis());
			return endTime;
		}
	}
	
	public String getEndTimeString() {
		Date endTime = getEndTime();
		
		if(endTime == null)
			return "-";
		else return TimeUtils.timeFormat.format(endTime.getTime());
	}
	
	public int getNumHours() {
		return numHours;
	}
	
	public void setNumHours(int numHours) {
		this.numHours = numHours;
	}
	
	public long getNumHoursAsMilis() {
		return this.numHours * 60 * 60 * 1000;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Date getCreateDate() {
		return createDate;
	}
	
	public String getCreateDateString() {
		return TimeUtils.shortDate.format(createDate);
	}
	
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public String getDisplayString() {
		StringBuilder buf = new StringBuilder();
		buf.append(getNumHours()+"hrs");
		if(getStartTime() != null) {
			buf.append("; "+getStartTimeString()+" to "+getEndTimeString());
		}
		
		return buf.toString();
	}
	
	public String getDisplayStringLong() {
		StringBuilder buf = new StringBuilder();
		buf.append(getNumHours()+"hrs");
		if(getStartTime() != null) {
			buf.append("; "+getStartTimeString()+" to "+getEndTimeString());
		}
		buf.append("; "+getName());
		
		return buf.toString();
	}
	
	public String getHtmlDisplayString() {
		StringBuilder buf = new StringBuilder();
		buf.append("<b>"+getNumHours()+"hrs</b>");
		if(getStartTime() != null) {
			buf.append("; "+getStartTimeString()+" to "+getEndTimeString());
		}
		
		return buf.toString();
	}
	
	public boolean getHasNoStartEndTime() {
		return (getStartTime() == null);
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("TimeBlock: ");
		buf.append("id: "+ getId());
		buf.append("; numHours: " + getNumHours());
		buf.append("; startTime: " + getStartTimeString());
		return buf.toString();
	}
	
}
