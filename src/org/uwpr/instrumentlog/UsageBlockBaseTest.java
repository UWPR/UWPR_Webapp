package org.uwpr.instrumentlog;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * getHours() must count elapsed hours.  A block across a daylight saving change counts one hour less or more
 * than its clock times.
 */
public class UsageBlockBaseTest extends TestCase {

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

    public final void testHoursWithNoClockChange() {
        UsageBlockBase block = block(2026, 9, 16, 22, 2026, 9, 17, 6);
        assertEquals("10 PM to 6 AM with no clock change should be 8 hours", 8, block.getHours());
    }

    public final void testHoursAcrossSpringForward() {
        // Clocks go from 2 AM to 3 AM on 2026-03-08.
        UsageBlockBase block = block(2026, 3, 7, 22, 2026, 3, 8, 6);
        assertEquals("10 PM to 6 AM across spring forward should be 7 hours", 7, block.getHours());
    }

    public final void testHoursAcrossFallBack() {
        // Clocks go from 2 AM back to 1 AM on 2026-11-01.
        UsageBlockBase block = block(2026, 10, 31, 22, 2026, 11, 1, 6);
        assertEquals("10 PM to 6 AM across fall back should be 9 hours", 9, block.getHours());
    }

    public final void testHoursOfAMultiDayBlockAcrossFallBack() {
        // Block 24951 on the dev copy, billed 29 hours.
        UsageBlockBase block = block(2025, 11, 1, 0, 2025, 11, 2, 4);
        assertEquals("Midnight Nov 1 to 4 AM Nov 2 2025, across fall back, should be 29 hours", 29, block.getHours());
    }

    private static UsageBlockBase block(int startYear, int startMonth, int startDay, int startHour,
                                        int endYear, int endMonth, int endDay, int endHour) {
        UsageBlockBase block = new UsageBlockBase();
        block.setStartDate(date(startYear, startMonth, startDay, startHour));
        block.setEndDate(date(endYear, endMonth, endDay, endHour));
        return block;
    }

    private static Date date(int year, int month, int day, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, hour, 0, 0);
        return calendar.getTime();
    }
}
