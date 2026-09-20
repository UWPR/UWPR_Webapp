package org.uwpr.scheduler;

import java.util.Arrays;
import java.util.Date;

import junit.framework.TestCase;

import org.yeastrc.project.payment.PaymentMethod;

/**
 * add() must refuse a percent that is not a whole number above 0, and one that takes the running total past
 * 100. checkPercents() must refuse percents that do not total 100. On the booking path these are the only
 * server-side checks on the percents.
 */
public class UsageBlockPaymentInformationTest extends TestCase {

    private static final Date END_DATE = new Date();

    private UsageBlockPaymentInformation paymentInfo;

    protected void setUp() throws Exception {
        super.setUp();
        paymentInfo = new UsageBlockPaymentInformation(Arrays.asList(paymentMethod(456), paymentMethod(457)));
    }

    public final void testTwoMethodsOverOneHundredPercent() throws SchedulerException {
        paymentInfo.add("456", "60", END_DATE);
        try {
            paymentInfo.add("457", "60", END_DATE);
            fail("60 and 60 percent should be refused as more than 100 percent");
        }
        catch (SchedulerException e) {
            assertEquals("60 and 60 percent should be refused as more than 100 percent",
                    "Total percent billed to individual payment methods cannot exceed 100%", e.getMessage());
        }
    }

    public final void testOneMethodOverOneHundredPercent() {
        try {
            paymentInfo.add("456", "999", END_DATE);
            fail("999 percent should be refused as more than 100 percent");
        }
        catch (SchedulerException e) {
            assertEquals("999 percent should be refused as more than 100 percent",
                    "Total percent billed to individual payment methods cannot exceed 100%", e.getMessage());
        }
    }

    public final void testRefusedMethodIsNotKept() throws SchedulerException {
        paymentInfo.add("456", "60", END_DATE);
        try {
            paymentInfo.add("457", "60", END_DATE);
            fail("60 and 60 percent should be refused as more than 100 percent");
        }
        catch (SchedulerException expected) {
            // checked by testTwoMethodsOverOneHundredPercent
        }
        assertEquals("A refused payment method should be left out of the list", 1, paymentInfo.getCount());
    }

    public final void testTwoMethodsAtOneHundredPercent() throws SchedulerException {
        paymentInfo.add("456", "60", END_DATE);
        paymentInfo.add("457", "40", END_DATE);
        assertEquals("60 and 40 percent should both be kept", 2, paymentInfo.getCount());
    }

    public final void testCheckPercentsUnderOneHundred() throws SchedulerException {
        paymentInfo.add("456", "50", END_DATE);
        try {
            paymentInfo.checkPercents();
            fail("A single payment method at 50 percent should be refused");
        }
        catch (SchedulerException e) {
            assertEquals("A single payment method at 50 percent should be refused",
                    "Total percent billed to individual payment methods must be 100%. Found 50%", e.getMessage());
        }
    }

    public final void testAddZeroPercent() throws SchedulerException {
        paymentInfo.add("456", "100", END_DATE);
        try {
            paymentInfo.add("457", "0", END_DATE);
            fail("A second payment method at 0 percent should be refused");
        }
        catch (SchedulerException e) {
            assertEquals("A second payment method at 0 percent should be refused",
                    "Percent billed to a payment method must be greater than 0. Found 0% for UW: 00-457", e.getMessage());
        }
    }

    /**
     * A negative percent leaves room under 100, so the running total would accept -50 followed by 150.
     */
    public final void testAddNegativePercent() {
        try {
            paymentInfo.add("456", "-50", END_DATE);
            fail("-50 percent should be refused");
        }
        catch (SchedulerException e) {
            assertEquals("-50 percent should be refused",
                    "Percent billed to a payment method must be greater than 0. Found -50% for UW: 00-456", e.getMessage());
        }
        assertEquals("A refused payment method should be left out of the list", 0, paymentInfo.getCount());
    }

    public final void testCheckPercentsOneHundred() throws SchedulerException {
        paymentInfo.add("456", "60", END_DATE);
        paymentInfo.add("457", "40", END_DATE);
        paymentInfo.checkPercents();
    }

    /**
     * 99.99 and 0.01 total 100, so only the percents themselves can refuse this split.
     */
    public final void testAddFractionalPercent() {
        try {
            paymentInfo.add("456", "99.99", END_DATE);
            fail("99.99 percent should be refused");
        }
        catch (SchedulerException e) {
            assertEquals("99.99 percent should be refused",
                    "Percent billed to a payment method must be a whole number. Found 99.99% for UW: 00-456",
                    e.getMessage());
        }
    }

    public final void testAddThirdsAreFractional() {
        try {
            paymentInfo.add("456", "33.33", END_DATE);
            fail("33.33 percent should be refused");
        }
        catch (SchedulerException e) {
            assertEquals("33.33 percent should be refused",
                    "Percent billed to a payment method must be a whole number. Found 33.33% for UW: 00-456",
                    e.getMessage());
        }
    }

    /**
     * instrumentUsagePayment.percentPayment is a decimal(5,2), so a percent read back from it carries two
     * decimal places. A whole number written that way must still be accepted.
     */
    public final void testAddWholeNumbersWithDecimalPlaces() throws SchedulerException {
        paymentInfo.add("456", "50.00", END_DATE);
        paymentInfo.add("457", "50.00", END_DATE);
        paymentInfo.checkPercents();
    }

    private static PaymentMethod paymentMethod(int id) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(id);
        paymentMethod.setUwbudgetNumber("00-" + id);
        return paymentMethod;
    }
}
