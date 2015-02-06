package org.yeastrc.www.taglib;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.uwpr.instrumentlog.DateUtils;
import org.uwpr.instrumentlog.InstrumentCalendar;
import org.uwpr.instrumentlog.InstrumentCalendar.Day;
import org.uwpr.instrumentlog.InstrumentCalendar.DayProject;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;

public class InstrumentCalendarTag extends TagSupport {

	private InstrumentCalendar calendar;
	
	// Name of the InstrumentCalendarTag bean that we want to render.
	public static final String CAL_BEAN_NAME = "calendar";
	
	public int doStartTag() throws JspException{
		try {
			if (CAL_BEAN_NAME == null) 
				throw new JspException("No dataBeanName specified for rendering instrumentCalendarTag");
			
			calendar = (InstrumentCalendar)pageContext.getRequest().getAttribute(CAL_BEAN_NAME);
			if (calendar == null)
				throw new JspException("No bean with name "+CAL_BEAN_NAME+" was found in the request");
			
			// Get our writer for the JSP page using this tag
			JspWriter writ = pageContext.getOut();
			writ.write(renderCalendar());
			
		}
		catch (IOException ioe) {
			throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
		}
		return SKIP_BODY;
	}

	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}

	public void release() {
		super.release();
	}
	
	public String renderCalendar() {
		StringBuilder buf = new StringBuilder();
		buf.append("<DIV style=\"font-weight: bold; font-size: 14px; margin-top: 10px;\">"+calendar.getInstrumentName()+" ("+calendar.getInstrumentID()+")</DIV>\n");
		buf.append("<DIV class=\"instrumentCalendar\">\n");
		buf.append("<TABLE width=\"100%\" height=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n");
		buf.append(getCalendarHeader());
		buf.append(getDaysHeader());
		buf.append(getDays(isAdminstrator()));
		buf.append("</TABLE>\n");
		buf.append("</DIV>\n");
		return buf.toString();
	}
	
	private boolean isAdminstrator() {
	    
	    HttpSession ses = pageContext.getSession();

	    // They are not authenticated
	    if (ses == null || ses.getAttribute("user") == null) {
	        return false;
	    }

	    User user = (User)(ses.getAttribute("user"));
	    Groups groupMan = Groups.getInstance();
	    int rID = user.getResearcher().getID();

	    // If the user is a member of "administrators", always execute the body
	    if (groupMan.isMember(rID, "administrators"))
	        return true;

	    return false;
	}
	
	private String getCalendarHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append("<TR class=\"header\">");
		buf.append("<TD style=\"padding: 0px; border-right: 0px;\"><a href=");
		buf.append(previousMonthURL());
		buf.append("style=\"text-decoration: none;\">");
		buf.append("<img src=\"/pr/images/prev_26.png\" alt=\"Prev\" border=0 align=\"middle\" /></a></TD>");
		buf.append("<TD colspan=\"5\" style=\"border-right: 0px;\" align=\"center\">");
		buf.append(calendar.getMonthName()+" "+calendar.getYear());
		buf.append("</TD>\n");
		buf.append("<TD style=\"padding: 0px\"><a href=");
		buf.append(nextMonthURL());
		buf.append(">");
		buf.append("<img src=\"/pr/images/next_26.png\" alt=\"Next\" border=0 align=\"middle\" /></a></TD>");
		buf.append("</TR>\n");
		return buf.toString();
	}
	
	private String previousMonthURL() {
		Date prevMonth = DateUtils.getPreviousMonth(calendar.getMonth(), calendar.getYear());
		return makeURL(DateUtils.getMonth(prevMonth), DateUtils.getYear(prevMonth));
	}
	
	private String nextMonthURL() {
		Date nextMonth = DateUtils.getNextMonth(calendar.getMonth(), calendar.getYear());
		return makeURL(DateUtils.getMonth(nextMonth), DateUtils.getYear(nextMonth));
	}
	
	private String makeURL(int month, int year) {
		StringBuilder buf = new StringBuilder();
		buf.append("\"");
		buf.append("/pr/viewInstrumentAvailability.do?instrumentID="+calendar.getInstrumentID());
		buf.append("&");
		buf.append("month="+month);
		buf.append("&");
		buf.append("year="+year);
		buf.append("\"");
		return buf.toString();
	}
	
	private String getDaysHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append("<TR class=\"daysHeader\">");
		for (int i = 1; i < 8; i++) {
			buf.append("<TD>");
			buf.append(InstrumentCalendar.DAYS[i]);
			buf.append("</TD>");
		}
		buf.append("</TR>\n");
		return buf.toString();
	}

	private String getDays(boolean isAdministrator) {
		StringBuilder buf = new StringBuilder();
		int day = 1;
		int maxDays = calendar.getDaysInCalendar();
		int firstWeekDay = calendar.getFirstWeekDay();
		
		int drawn = 0;
		buf.append(beginDateRow());
		
		// draw any blank cells in the beginning
		for (int i = 1; i < firstWeekDay; i++) {
			buf.append(emptyCell());
			drawn++;
		}
		
		while (day <= maxDays) {
			if (drawn == 7) {
				buf.append(endDateRow());
				buf.append(beginDateRow());
				drawn = 0;
			}
			if (calendar.busyDay(day)) {
				buf.append(busyDay(calendar.getBusyDay(day), isAdministrator));
			}
			else
				buf.append(freeDay(day));
			drawn++;
			day++;
		}
		
		// draw any blank cells at the end
		for (int i = drawn; i < 7; i++) {
			buf.append(emptyCell());
		}
		buf.append(endDateRow());
		
		return buf.toString();
	}
	
	private String beginDateRow() {
		return "<TR>\n";
	}
	
	private String endDateRow() {
		return "</TR>\n";
	}
	
	private String emptyCell() {
		return "<TD class=\"empty\">a</TD>\n";
	}
	
	private String freeDay(int day) {
		StringBuilder buf = new StringBuilder();
		buf.append("<TD class=\"free\">");
		buf.append(day);
		buf.append("</TD>\n");
		return buf.toString();
	}
	
	private String busyDay(Day day, boolean isAdministrator) {
		StringBuilder buf = new StringBuilder();
		buf.append("<TD class=\"busy\">");
		buf.append(day.getDayOfMonth());
		buf.append("<DIV class=\"busyData\">");
		List<DayProject> projects = day.getProjects();
		for (int i = 0; i < projects.size(); i++) {
			if (i > 0)
				buf.append(", ");
			// only administrators should be able to link to projects
			if(isAdministrator)
			    buf.append(makeProjectLink(projects.get(i)));
			else
			    buf.append(projects.get(i).getProjectId());
		}
		buf.append("</DIV>");
		buf.append("</TD>\n");
		return buf.toString();
	}
	
	private String makeProjectLink(DayProject project) {
	    StringBuilder buf = new StringBuilder();
        buf.append("<span class=\"tooltip\" title=\"");
        buf.append("<b>Project ID:</b> "+project.getProjectId());
        buf.append(" <br> ");
        buf.append("<b>Title:</b> "+project.getTitle());
        buf.append(" <br> ");
        buf.append("<b>PI:</b> "+project.getPiFirstName()+"&nbsp;"+project.getPiLastName());
        buf.append("\" ");
        buf.append(">");
        buf.append("<a href=\"/pr/viewProject.do?ID="+project.getProjectId()+"\">");
        buf.append(project.getProjectId());
        buf.append("</a>");
        buf.append("</span>");
        return buf.toString();
	}
}
