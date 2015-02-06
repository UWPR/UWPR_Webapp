package org.uwpr.instrumentlog;

import java.util.Date;
import java.util.Calendar;

import junit.framework.TestCase;

public class DateUtilsTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testGetNumDays() {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.YEAR, 2009);
        calendar1.set(Calendar.MONTH, 0); // January
        calendar1.set(Calendar.DAY_OF_MONTH, 23);
        calendar1.set(Calendar.HOUR_OF_DAY, 22);
        
        Date startDate = new Date(calendar1.getTimeInMillis());
        
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.YEAR, 2009);
        calendar2.set(Calendar.MONTH, 2); // March
        calendar2.set(Calendar.DAY_OF_MONTH, 9); // past DST day
        calendar2.set(Calendar.HOUR_OF_DAY, 5);
        
        Date endDate = new Date(calendar2.getTimeInMillis());
        
        assertEquals(46, DateUtils.getNumDays(startDate, endDate));
        
        
        calendar1.set(Calendar.YEAR, 2008); // Leap year
        calendar1.set(Calendar.MONTH, 0);   // January
        calendar1.set(Calendar.DAY_OF_MONTH, 23);
        calendar1.set(Calendar.HOUR_OF_DAY, 22);
        startDate = new Date(calendar1.getTimeInMillis());
        
        calendar2.set(Calendar.YEAR, 2009);
        calendar2.set(Calendar.MONTH, 2); // March
        calendar2.set(Calendar.DAY_OF_MONTH, 9); // past DST day
        calendar2.set(Calendar.HOUR_OF_DAY, 5);
        endDate = new Date(calendar2.getTimeInMillis());
        
        assertEquals((46+366), DateUtils.getNumDays(startDate, endDate));
        
    }

    public final void testGetNumUsableHours() {
        
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.YEAR, 2009);
        calendar1.set(Calendar.MONTH, 0); // January
        calendar1.set(Calendar.DAY_OF_MONTH, 23);
        calendar1.set(Calendar.HOUR_OF_DAY, 22);
        calendar1.clear(Calendar.MINUTE);
        calendar1.clear(Calendar.SECOND);
        calendar1.clear(Calendar.MILLISECOND);
        
        Date startDate = new Date(calendar1.getTimeInMillis());
        
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.YEAR, 2009);
        calendar2.set(Calendar.MONTH, 2);        // March
        calendar2.set(Calendar.DAY_OF_MONTH, 9); // past DST day
        calendar2.set(Calendar.HOUR_OF_DAY, 6);
        calendar2.set(Calendar.MINUTE, 30);
        calendar2.clear(Calendar.SECOND);
        calendar2.clear(Calendar.MILLISECOND);
        
        Date endDate = new Date(calendar2.getTimeInMillis());
        
        assertEquals((float)(44*24 + 8.5), DateUtils.getNumUsableHours(startDate, endDate));
        
        calendar2.set(Calendar.HOUR_OF_DAY, 19);
        endDate = new Date(calendar2.getTimeInMillis());
        assertEquals((float)(44*24 + 21.5), DateUtils.getNumUsableHours(startDate, endDate));
        
        calendar1.set(Calendar.HOUR_OF_DAY, 7);
        startDate = new Date(calendar1.getTimeInMillis());
        assertEquals((float)(45*24 + 12.5), DateUtils.getNumUsableHours(startDate, endDate));
        
       
        calendar1.set(Calendar.HOUR_OF_DAY, 9);
        calendar1.set(Calendar.MINUTE, 30);
        startDate = new Date(calendar1.getTimeInMillis());
        assertEquals((float)(45*24 +10), DateUtils.getNumUsableHours(startDate, endDate));
    }
    
    public final void testGetNumUsableHours2() {
        
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.YEAR, 2011);
        calendar1.set(Calendar.MONTH, 9);  // October
        calendar1.set(Calendar.DAY_OF_MONTH, 3);
        calendar1.set(Calendar.HOUR_OF_DAY, 8);
        calendar1.clear(Calendar.MINUTE);
        calendar1.clear(Calendar.SECOND);
        calendar1.clear(Calendar.MILLISECOND);
        
        Date startDate = new Date(calendar1.getTimeInMillis());
        
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.YEAR, 2011);
        calendar2.set(Calendar.MONTH, 10);        // November
        calendar2.set(Calendar.DAY_OF_MONTH, 2); 
        calendar2.set(Calendar.HOUR_OF_DAY, 18);
        calendar2.clear(Calendar.MINUTE);
        calendar2.clear(Calendar.SECOND);
        calendar2.clear(Calendar.MILLISECOND);
        
        Date endDate = new Date(calendar2.getTimeInMillis());
        
        assertEquals((float)(730.0), DateUtils.getNumUsableHours(startDate, endDate));
        
    }

    
//    public final void testGetNumUsableHours2() {
//        
//        Date startDate = DateUtils.currentMinusDays(59);
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(startDate);
//        assertEquals(10.0f, DateUtils.usableHoursStarting(cal));
//        
//        Date endDate = DateUtils.currentPlusDays(30, true);
//        
//        assertEquals(90, DateUtils.getNumDays(startDate, endDate));
//        assertEquals((float)(900.0), DateUtils.getNumUsableHours(startDate, endDate));
//    }
    
    
    public final void testGetNumHoursUsed() {
        
        // 2009-04-19 00:00:00.0 - 2009-05-01 00:00:00.0  
        Date startDate = DateUtils.getDate(19, 4, 2009);
        Date endDate = DateUtils.getDate(1, 5, 2009);
        assertEquals(13, DateUtils.getNumDays(startDate, endDate));
        assertEquals((float)((12*24)), DateUtils.getNumHoursUsed(startDate, endDate));
        
        // 2009-04-19 08:00:00.0 - 2009-05-01 18:00:00.0
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date newStartDate = new Date(cal.getTimeInMillis());
        
        cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date newEndDate = new Date(cal.getTimeInMillis());
        
        assertEquals(13, DateUtils.getNumDays(newStartDate, newEndDate));
        assertEquals((float)(10+(12*24)), DateUtils.getNumHoursUsed(newStartDate, newEndDate));
        
    }
    
    public final void testGetNumHours() {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.YEAR, 2009);
        calendar1.set(Calendar.MONTH, 0);       // January
        calendar1.set(Calendar.DAY_OF_MONTH, 23);
        calendar1.set(Calendar.HOUR_OF_DAY, 22);
        calendar1.set(Calendar.MINUTE, 0);
        
        Date startDate = new Date(calendar1.getTimeInMillis());
        
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.YEAR, 2009);
        calendar2.set(Calendar.MONTH, 1);       // February
        calendar2.set(Calendar.DAY_OF_MONTH,9); 
        calendar2.set(Calendar.HOUR_OF_DAY, 7);
        calendar2.set(Calendar.MINUTE, 30);
        
        Date endDate = new Date(calendar2.getTimeInMillis());
        
        assertEquals((float)(16*24+(2+7+0.5)), DateUtils.getNumHours(startDate, endDate));
        
        calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.YEAR, 2009);
        calendar1.set(Calendar.MONTH, 0); // January
        calendar1.set(Calendar.DAY_OF_MONTH, 23);
        calendar1.set(Calendar.HOUR_OF_DAY, 22);
        calendar1.set(Calendar.MINUTE, 0);
        
        startDate = new Date(calendar1.getTimeInMillis());
        
        calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.YEAR, 2009);
        calendar2.set(Calendar.MONTH, 2);       // March
        calendar2.set(Calendar.DAY_OF_MONTH,9); // past DST day
        calendar2.set(Calendar.HOUR_OF_DAY, 7);
        calendar2.set(Calendar.MINUTE, 30);
        
        endDate = new Date(calendar2.getTimeInMillis());
        assertEquals((float)(44*24+(2+7+0.5)), DateUtils.getNumHours(startDate, endDate));
    }

}
