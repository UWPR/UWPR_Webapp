package org.uwpr.www;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.uwpr.www.ScheduledTask.ScheduledTimerTask;

import junit.framework.TestCase;

public class ScheduledTimerTaskTest extends TestCase {

	private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public void testExpireProject() {
		
		ScheduledTask task = new ScheduledTask();
		ScheduledTimerTask timerTask = task.new ScheduledTimerTask(null);
		
		
		String acceptDateString = "2010-07-29 16:03:59";
		
		String currentDateString = "2011-07-22 16:03:59";
		
		Date currentDate = null;
		Date acceptDate = null; 
		
		try {
			currentDate = format.parse(currentDateString);
			acceptDate = format.parse(acceptDateString);
		} catch (ParseException e) {
			
			fail(e.getMessage());
		}
		
		assertFalse(timerTask.expireProject(acceptDate, currentDate));
		
		currentDateString = "2011-07-29 16:03:59";
		try {
			currentDate = format.parse(currentDateString);
		} catch (ParseException e) {
			
			fail(e.getMessage());
		}
		
		assertTrue(timerTask.expireProject(acceptDate, currentDate));
		
		
		// go over a leap year
		acceptDateString = "2011-07-29 16:03:59";
		currentDateString = "2012-07-28 16:03:59";
		try {
			currentDate = format.parse(currentDateString);
			acceptDate = format.parse(acceptDateString);
		} catch (ParseException e) {
			
			fail(e.getMessage());
		}
		assertFalse(timerTask.expireProject(acceptDate, currentDate));
		
		
		currentDateString = "2012-07-29 16:03:59";
		try {
			currentDate = format.parse(currentDateString);
			acceptDate = format.parse(acceptDateString);
		} catch (ParseException e) {
			
			fail(e.getMessage());
		}
		assertTrue(timerTask.expireProject(acceptDate, currentDate));
		
	}

}
