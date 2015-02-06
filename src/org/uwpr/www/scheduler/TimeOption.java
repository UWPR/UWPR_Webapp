/**
 * TimeOption.java
 * @author Vagisha Sharma
 * Jan 8, 2012
 */
package org.uwpr.www.scheduler;

import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;

import java.util.ArrayList;
import java.util.List;

public final class TimeOption {
	private int value;
	private String display = "";
	public TimeOption(int value, String display) {
		this.value = value;
		this.display = display;
	}
	public int getValue() {
		return value;
	}
	public String getDisplay() {
		return display;
	}

	public static List<TimeOption> getStartTimeOptions(User user) {

		boolean isAdmin = false;
		Groups groups = Groups.getInstance();
		if(groups.isMember(user.getResearcher().getID(), "administrators")) {
			isAdmin = true;
		}

		List<TimeOption> options = new ArrayList<TimeOption>();
		if(isAdmin) {
            getAdminTimeOptions(options);
		}
		else {
			options.add(new TimeOption(10, "10:00 am"));
			options.add(new TimeOption(14, "2:00 pm"));
		}
		return options;
	}

    private static void getAdminTimeOptions(List<TimeOption> options) {
        options.add(new TimeOption(0,"12:00 am"));
        for(int i = 1; i <= 11; i++) {
            options.add(new TimeOption(i,i+":00 am"));
        }
        options.add(new TimeOption(12, "12:00 pm"));
        for(int i = 13; i <= 23; i++) {
            options.add(new TimeOption(i,(i-12)+":00 pm"));
        }
    }

    public static List<TimeOption> getEndTimeOptions(User user) {

        boolean isAdmin = false;
        Groups groups = Groups.getInstance();
        if(groups.isMember(user.getResearcher().getID(), "administrators")) {
            isAdmin = true;
        }

        List<TimeOption> options = new ArrayList<TimeOption>();
        if(isAdmin) {
            getAdminTimeOptions(options);
        }
        else {
            options.add(new TimeOption(10, "10:00 am"));
            options.add(new TimeOption(14, "2:00 pm"));
            options.add(new TimeOption(18, "6:00 pm"));
        }
        return options;
    }
}