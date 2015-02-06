/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package org.uwpr.www.instrumentlog;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.instrumentlog.DateUtils;


public class InstrumentUsageForm extends ActionForm {
	/*
	 * Generated fields
	 */

	/** projectID property */
	private int projectID;

	/** startDateMonth property */
	private int startDateMonth;

	/** startDateYear property */
	private int startDateYear;

	/** startDateDay property */
	private int startDateDay;
	
	private int startHour;
	private int startMin;
	private int startAmPm;
	
	/** endDateMonth property */
	private int endDateMonth;

	/** endDateYear property */
	private int endDateYear;

	/** endDateDay property */
	private int endDateDay;
	
	private int endHour;
	private int endMin;
	private int endAmPm;

	/** instumentID property */
	private int instrumentID = 0;
	
	/** notes property */
	private String notes;

	private int usageID = 0;
	
	/*
	 * Generated Methods
	 */

	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();
		if (projectID == 0) {
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.add.noproject"));
		}
		if (instrumentID == 0) {
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.add.noinstrument"));
		}
		
		Date startDate = DateUtils.getDate(startDateDay, startDateMonth, startDateYear);
		Date endDate = DateUtils.getDate(endDateDay, endDateMonth, endDateYear);
		
		if (startDate == null)
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.startdate"));
		if (endDate == null)
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.enddate"));
		
		if ((startDate != null && endDate != null ) && startDate.compareTo(endDate) == 1) 
			errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.daterange"));
		
		// the user may have entered a day that is greater than the max days in the month. 
		if(startDate != null) {
			int startDateDay_c = DateUtils.getDay(startDate);
			if(startDateDay_c != startDateDay) {
				errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.startdate"));
			}
		}
		if(endDate != null) {
			int endDateDay_c = DateUtils.getDay(endDate);
			if(endDateDay_c != endDateDay) {
				errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.enddate"));
			}
		}
		
		return errors;
	}

	/** 
	 * Returns the projectID.
	 * @return int
	 */
	public int getProjectID() {
		return projectID;
	}

	/** 
	 * Set the projectID.
	 * @param projectID The projectID to set
	 */
	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}

	/** 
	 * Returns the startDateMonth.
	 * @return int
	 */
	public int getStartDateMonth() {
		return startDateMonth;
	}

	/** 
	 * Set the startDateMonth.
	 * @param startDateMonth The startDateMonth to set
	 */
	public void setStartDateMonth(int startDateMonth) {
		this.startDateMonth = startDateMonth;
	}

	/** 
	 * Returns the startDateYear.
	 * @return int
	 */
	public int getStartDateYear() {
		return startDateYear;
	}

	/** 
	 * Set the startDateYear.
	 * @param startDateYear The startDateYear to set
	 */
	public void setStartDateYear(int startDateYear) {
		this.startDateYear = startDateYear;
	}

	/** 
	 * Returns the startDateDay.
	 * @return int
	 */
	public int getStartDateDay() {
		return startDateDay;
	}

	/** 
	 * Set the startDateDay.
	 * @param startDateDay The startDateDay to set
	 */
	public void setStartDateDay(int startDateDay) {
		this.startDateDay = startDateDay;
	}

	/** 
	 * Returns the endDateMonth.
	 * @return int
	 */
	public int getEndDateMonth() {
		return endDateMonth;
	}

	/** 
	 * Set the endDateMonth.
	 * @param endDateMonth The endDateMonth to set
	 */
	public void setEndDateMonth(int endDateMonth) {
		this.endDateMonth = endDateMonth;
	}

	/** 
	 * Returns the endDateYear.
	 * @return int
	 */
	public int getEndDateYear() {
		return endDateYear;
	}

	/** 
	 * Set the endDateYear.
	 * @param endDateYear The endDateYear to set
	 */
	public void setEndDateYear(int endDateYear) {
		this.endDateYear = endDateYear;
	}

	/** 
	 * Returns the endDateDay.
	 * @return int
	 */
	public int getEndDateDay() {
		return endDateDay;
	}

	/** 
	 * Set the endDateDay.
	 * @param endDateDay The endDateDay to set
	 */
	public void setEndDateDay(int endDateDay) {
		this.endDateDay = endDateDay;
	}
	
	/** 
	 * Returns the instumentID.
	 * @return int
	 */
	public int getInstrumentID() {
		return instrumentID;
	}

	/** 
	 * Set the instumentID.
	 * @param instumentID The instumentID to set
	 */
	public void setInstrumentID(int instumentID) {
		this.instrumentID = instumentID;
	}
	
	/** 
	 * Returns the notes.
	 * @return String
	 */
	public String getNotes() {
		return notes;
	}
	
	/** 
	 * Set the notes.
	 * @param notes The notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public int getUsageID() {
		return usageID;
	}
	
	public void setUsageID(int id) {
		this.usageID = id;
	}

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public String getStartHourFmt() {
        if(startHour < 10)
            return "0"+startHour;
        return startHour+"";
    }
    
    public void setStartHourFmt(String startHour) {
        setStartHour(Integer.parseInt(startHour));
    }
    
    public int getStartMin() {
        return startMin;
    }
    
    public String getStartMinFmt() {
        if(startMin < 10)
            return "0"+startMin;
        return startMin+"";
    }
    
    public void setStartMinFmt(String min) {
        setStartMin(Integer.parseInt(min));
    }

    public void setStartMin(int startMin) {
        this.startMin = startMin;
    }

    public int getStartAmPm() {
        return startAmPm;
    }

    public void setStartAmPm(int startAmPm) {
        this.startAmPm = startAmPm;
    }

    public int getEndHour() {
        return endHour;
    }

    public String getEndHourFmt() {
        if(endHour < 10)
            return "0"+endHour;
        return endHour+"";
    }
    
    public void setEndHourFmt(String endHour) {
        setEndHour(Integer.parseInt(endHour));
    }
    
    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMin() {
        return endMin;
    }

    public void setEndMin(int endMin) {
        this.endMin = endMin;
    }
    
    public String getEndMinFmt() {
        if(endMin < 10)
            return "0"+endMin;
        return endMin+"";
    }
    
    public void setEndMinFmt(String min) {
        setEndMin(Integer.parseInt(min));
    }

    public int getEndAmPm() {
        return endAmPm;
    }

    public void setEndAmPm(int endAmPm) {
        this.endAmPm = endAmPm;
    }
}