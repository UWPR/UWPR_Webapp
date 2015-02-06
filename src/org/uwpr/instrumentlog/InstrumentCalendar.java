package org.uwpr.instrumentlog;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.InvalidProjectTypeException;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;

public class InstrumentCalendar {

	private final int year;
	private final int month;
	private final String instrumentName;
	private final int instrumentID;
	private final TreeMap <Integer, Day> days;
	
	private static final String[] MONTHS = new String[] {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	public static final String[] DAYS = new String[8];
	static {
		DAYS[0] = "";
		DAYS[Calendar.SUNDAY] = "Sun";
		DAYS[Calendar.MONDAY] = "Mon";
		DAYS[Calendar.TUESDAY] = "Tue";
		DAYS[Calendar.WEDNESDAY] = "Wed";
		DAYS[Calendar.THURSDAY] = "Thu";
		DAYS[Calendar.FRIDAY] = "Fri";
		DAYS[Calendar.SATURDAY] = "Sat";
	}
	
	/**
	 * @param year YYYY 
	 * @param month -- 0 to 11 where January is 0 and December is 11
	 * @param instrumentName
	 * @param instrumentID
	 */
	public InstrumentCalendar(int year, int month, String instrumentName, int instrumentID) {
		this.year = year;
		this.month = month;
		this.instrumentID = instrumentID;
		this.instrumentName = instrumentName;
		days = new TreeMap<Integer, Day>();
	}
	
	public void addBusyDay(int day, int projectID) throws InvalidProjectTypeException, SQLException, InvalidIDException {
		Day busyDay = getDay(day);
		Project proj = ProjectFactory.getProject(projectID);
		DayProject dProj = new DayProject(projectID);
		dProj.setTitle(proj.getTitle());
		dProj.setPiFirstName(proj.getPI().getFirstName());
		dProj.setPiLastName(proj.getPI().getLastName());
		busyDay.addProject(dProj);
		days.put(day, busyDay);
	}

	private Day getDay(int day) {
		Day busyDay = days.get(day);
		if (busyDay == null)
			busyDay = new Day(day, getDayOfWeek(day));
		return busyDay;
	}
	
	private int getDayOfWeek(int day) {
		return DateUtils.getDayOfWeek(day, month, year);
	}

	public int getFirstWeekDay() {
		return getDayOfWeek(1);
	}
	
	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}

	public String getMonthName() {
		return MONTHS[month];
	}
	
	/**
	 * @return the instrumentName
	 */
	public String getInstrumentName() {
		return instrumentName;
	}

	/**
	 * @return the instrumentID
	 */
	public int getInstrumentID() {
		return instrumentID;
	}

	/**
	 * @return the days
	 */
	public TreeMap<Integer, Day> getDays() {
		return days;
	}
	
	public int getDaysInCalendar() {
		return DateUtils.getDaysInMonth(month, year);
	}
	
	public int getNumBusyDays() {
		return days.size();
	}
	
	public boolean busyDay(int day) {
		return days.get(day) != null;
	}
	
	public Day getBusyDay(int day) {
		return days.get(day);
	}
	
	public static class Day {
		
		private int dayOfMonth;
		private int dayOfWeek;
		private List <DayProject> projects;
		
		public Day(int dayOfMonth, int dayOfWeek) {
			this.dayOfMonth = dayOfMonth;
			this.dayOfWeek = dayOfWeek;
			projects = new ArrayList<DayProject>();
		}
		
		public void addProject(DayProject project) {
			if (!projects.contains(project))
				projects.add(project);
		}

		/**
		 * @return the dayOfMonth
		 */
		public int getDayOfMonth() {
			return dayOfMonth;
		}

		/**
		 * @return the dayOfWeek
		 */
		public int getDayOfWeek() {
			return dayOfWeek;
		}

		/**
		 * @return the projectIDs
		 */
		public List<Integer> getProjectIDs() {
			List<Integer> ids = new ArrayList<Integer>(projects.size());
			for (DayProject project: projects) {
			    ids.add(project.getProjectId());
	        }
			return ids;
		}
		
		public List<DayProject> getProjects() {
		    return projects;
		}
		
		public String toString() {
			return dayOfMonth+"; "+DAYS[dayOfWeek]+"; "+projects;
		}
		
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder(MONTHS[month]+" "+year);
		buf.append("# busy days: "+getNumBusyDays()+"\n");
		for (Day day: days.values()) {
			buf.append(day.toString());
			buf.append("\n");
		}
		for (int i = 0; i < DAYS.length; i++)
			buf.append(DAYS[i]);
		return buf.toString();
	}
	
	public static class DayProject {
	    
	    private final int projectId;
	    private String title;
	    private String piFirstName;
	    private String piLastName;
	    
	    public DayProject(int projectId) {
	        this.projectId = projectId;
	    }
	    
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getPiFirstName() {
            return piFirstName;
        }
        public void setPiFirstName(String piFirstName) {
            this.piFirstName = piFirstName;
        }
        public String getPiLastName() {
            return piLastName;
        }
        public void setPiLastName(String piLastName) {
            this.piLastName = piLastName;
        }
        public int getProjectId() {
            return projectId;
        }
	}
}
