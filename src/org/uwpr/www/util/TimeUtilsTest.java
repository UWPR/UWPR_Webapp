package org.uwpr.www.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * makeEndOfDay_12AM() is the end boundary of the billing export and the scheduled-time views, so it must be
 * midnight at the start of the next day, including on a day when the clocks change.
 */
public class TimeUtilsTest extends TestCase {

    private TimeZone defaultZone;

    protected void setUp() throws Exception {
        super.setUp();
        defaultZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    protected void tearDown() throws Exception {
        TimeZone.setDefault(defaultZone);
        super.tearDown();
    }

    public final void testEndOfAnOrdinaryDay() {
        assertEquals("The end of 2026-09-17 should be midnight on 2026-09-18",
                "2026-09-18 00:00", format(TimeUtils.makeEndOfDay_12AM(date(2026, 9, 17, 10))));
    }

    public final void testEndOfSpringForwardDay() {
        assertEquals("The end of 2026-03-08, when clocks go forward, should be midnight on 2026-03-09",
                "2026-03-09 00:00", format(TimeUtils.makeEndOfDay_12AM(date(2026, 3, 8, 10))));
    }

    public final void testEndOfFallBackDay() {
        assertEquals("The end of 2026-11-01, when clocks go back, should be midnight on 2026-11-02",
                "2026-11-02 00:00", format(TimeUtils.makeEndOfDay_12AM(date(2026, 11, 1, 10))));
    }

    private static String format(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    private static Date date(int year, int month, int day, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, hour, 0, 0);
        return calendar.getTime();
    }
}
