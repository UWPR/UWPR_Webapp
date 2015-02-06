package org.uwpr.www.instrumentlog;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.instrumentlog.DateUtils;

public class DateRangeForm extends ActionForm {

	/** startDateMonth property */
	private Integer startDateMonth;

	/** startDateYear property */
	private Integer startDateYear;

	/** startDateDay property */
	private Integer startDateDay;
	
	/** endDateMonth property */
	private Integer endDateMonth;

	/** endDateYear property */
	private Integer endDateYear;

	/** endDateDay property */
	private Integer endDateDay;
	
	private int instrumentID = -1;
	
	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();
		
		if(isInitialized()) {
			Date startDate = DateUtils.getDate(startDateDay, startDateMonth, startDateYear);
			Date endDate = DateUtils.getDate(endDateDay, endDateMonth, endDateYear);

			if (startDate == null)
				errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.startdate"));
			else if (endDate == null)
				errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.enddate"));

			else if (startDate.compareTo(endDate) == 1) 
				errors.add("instrumentlog", new ActionMessage("error.instrumentlog.invalid.daterange"));

			// the user may have entered a day that is greater than the max days in the month. 
			// If that is the case we set the day to the last day of the month
			if(startDate != null)
				startDateDay = DateUtils.getDay(startDate);
			if(endDate != null)
				endDateDay = DateUtils.getDay(endDate);
		}
		return errors;
	}

	/** 
	 * Returns the startDateMonth.
	 * @return Integer
	 */
	public Integer getStartDateMonth() {
		return startDateMonth;
	}

	/** 
	 * Set the startDateMonth.
	 * @param startDateMonth The startDateMonth to set
	 */
	public void setStartDateMonth(Integer startDateMonth) {
		this.startDateMonth = startDateMonth;
	}

	/** 
	 * Returns the startDateYear.
	 * @return Integer
	 */
	public Integer getStartDateYear() {
		return startDateYear;
	}

	/** 
	 * Set the startDateYear.
	 * @param startDateYear The startDateYear to set
	 */
	public void setStartDateYear(Integer startDateYear) {
		this.startDateYear = startDateYear;
	}

	/** 
	 * Returns the startDateDay.
	 * @return Integer
	 */
	public Integer getStartDateDay() {
		return startDateDay;
	}

	/** 
	 * Set the startDateDay.
	 * @param startDateDay The startDateDay to set
	 */
	public void setStartDateDay(Integer startDateDay) {
		this.startDateDay = startDateDay;
	}

	/** 
	 * Returns the endDateMonth.
	 * @return Integer
	 */
	public Integer getEndDateMonth() {
		return endDateMonth;
	}

	/** 
	 * Set the endDateMonth.
	 * @param endDateMonth The endDateMonth to set
	 */
	public void setEndDateMonth(Integer endDateMonth) {
		this.endDateMonth = endDateMonth;
	}

	/** 
	 * Returns the endDateYear.
	 * @return Integer
	 */
	public Integer getEndDateYear() {
		return endDateYear;
	}

	/** 
	 * Set the endDateYear.
	 * @param endDateYear The endDateYear to set
	 */
	public void setEndDateYear(Integer endDateYear) {
		this.endDateYear = endDateYear;
	}

	/** 
	 * Returns the endDateDay.
	 * @return Integer
	 */
	public Integer getEndDateDay() {
		return endDateDay;
	}

	/** 
	 * Set the endDateDay.
	 * @param endDateDay The endDateDay to set
	 */
	public void setEndDateDay(Integer endDateDay) {
		this.endDateDay = endDateDay;
	}
	
	public int getInstrumentID() {
		return instrumentID;
	}
	
	public void setInstrumentID(int id) {
		this.instrumentID = id;
	}
	
	public boolean isInitialized() {
		if (startDateDay == null || startDateMonth == null || startDateYear == null ||
			endDateDay == null || endDateMonth == null || endDateYear == null)
			return false;
		return true;
	}
}
