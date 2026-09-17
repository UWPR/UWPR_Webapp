package org.uwpr.www.instrumentlog;

import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * formatEventTime() must return a block time as the wall-clock time in the server's zone, the zone the block's
 * label is formatted in, with no zone in the string.  Each test starts from a fixed moment in UTC, so the
 * expected hour depends on the server's zone.
 */
public class JSONInstrumentUsageGetterTest extends TestCase {

    private TimeZone defaultZone;

    protected void setUp() throws Exception {
        super.setUp();
        defaultZone = TimeZone.getDefault();
    }

    protected void tearDown() throws Exception {
        TimeZone.setDefault(defaultZone);
        super.tearDown();
    }

    public final void testEventTimeOnALosAngelesServer() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
        assertEquals("17:00 UTC on 2026-09-17 should be sent as 10:00 from a Los Angeles server (UTC - 7)",
                "2026-09-17T10:00:00", JSONInstrumentUsageGetter.formatEventTime(utc("2026-09-17T17:00:00Z")));
    }

    public final void testEventTimeOnALosAngelesServerAfterFallBack() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
        assertEquals("14:00 UTC on 2026-11-01, after the clocks go back, "
                        + "should be sent as 06:00 from a Los Angeles server (UTC - 8)",
                "2026-11-01T06:00:00", JSONInstrumentUsageGetter.formatEventTime(utc("2026-11-01T14:00:00Z")));
    }

    public final void testEventTimeOnANewYorkServer() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
        assertEquals("17:00 UTC on 2026-09-17 should be sent as 13:00 from a New York server (UTC - 4)",
                "2026-09-17T13:00:00", JSONInstrumentUsageGetter.formatEventTime(utc("2026-09-17T17:00:00Z")));
    }

    public final void testEventTimeOnAKolkataServer() {
        // Kolkata has no daylight saving time and a half-hour offset from UTC.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        assertEquals("17:00 UTC on 2026-09-17 should be sent as 22:30 from a Kolkata server (UTC + 5:30)",
                "2026-09-17T22:30:00", JSONInstrumentUsageGetter.formatEventTime(utc("2026-09-17T17:00:00Z")));
    }

    public final void testEventTimeOnAServerWithAFixedOffset() {
        // A fixed offset has no daylight saving time, so in September it is an hour behind Pacific time.
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-08:00"));
        assertEquals("17:00 UTC on 2026-09-17 should be sent as 09:00 from a GMT-08:00 server (UTC - 8)",
                "2026-09-17T09:00:00", JSONInstrumentUsageGetter.formatEventTime(utc("2026-09-17T17:00:00Z")));
    }

    private static Date utc(String isoInstant) {
        return Date.from(Instant.parse(isoInstant));
    }
}
