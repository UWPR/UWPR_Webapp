package org.uwpr;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Guards the mail timeout read from config_msdapl_webapp. JavaMail reads 0 as no timeout, which is
 * the setting that let a send block a request thread for about 130 seconds.
 */
public class AppPropertiesTest extends TestCase
{
    public final void testTimeoutFromConfig()
    {
        assertEquals("A positive mail.smtp.timeout should be used as configured",
                30000, mailProps("30000").getTimeoutMillis());
    }

    public final void testTimeoutIsTrimmed()
    {
        assertEquals("A mail.smtp.timeout with surrounding spaces should be used as configured",
                30000, mailProps("  30000  ").getTimeoutMillis());
    }

    public final void testMissingTimeoutUsesDefault()
    {
        Map<String, String> config = new HashMap<>();
        config.put("mail.smtp.host", "smtp.example.org");
        // The literal rather than the constant, so lowering DEFAULT_TIMEOUT_MILLIS to a value
        // JavaMail reads as no timeout fails here.
        assertEquals("A configuration with no mail.smtp.timeout should use a 15 second default",
                15000,
                new AppProperties.MailProperties(config).getTimeoutMillis());
    }

    public final void testNonNumericTimeoutUsesDefault()
    {
        assertEquals("A mail.smtp.timeout that is not a number should use the default",
                AppProperties.MailProperties.DEFAULT_TIMEOUT_MILLIS,
                mailProps("fifteen seconds").getTimeoutMillis());
    }

    public final void testZeroTimeoutUsesDefault()
    {
        assertEquals("A mail.smtp.timeout of 0 should use the default",
                AppProperties.MailProperties.DEFAULT_TIMEOUT_MILLIS,
                mailProps("0").getTimeoutMillis());
    }

    public final void testNegativeTimeoutUsesDefault()
    {
        assertEquals("A negative mail.smtp.timeout should use the default",
                AppProperties.MailProperties.DEFAULT_TIMEOUT_MILLIS,
                mailProps("-1").getTimeoutMillis());
    }

    public final void testOtherMailPropertiesAreStillRead()
    {
        Map<String, String> config = new HashMap<>();
        config.put("mail.smtp.host", "smtp.example.org");
        config.put("mail.smtp.port", "587");
        config.put("from.email.address", "sender@example.org");
        config.put("from.email.password", "secret");

        AppProperties.MailProperties mailProps = new AppProperties.MailProperties(config);
        assertEquals("The SMTP host should be read from the configuration",
                "smtp.example.org", mailProps.getSmtpHost());
        assertEquals("The SMTP port should be read from the configuration",
                "587", mailProps.getSmtpPort());
        assertEquals("The sender address should be read from the configuration",
                "sender@example.org", mailProps.getSenderEmail());
        assertEquals("The sender password should be read from the configuration",
                "secret", mailProps.getSenderPassword());
    }

    private static AppProperties.MailProperties mailProps(String timeout)
    {
        Map<String, String> config = new HashMap<>();
        config.put("mail.smtp.timeout", timeout);
        return new AppProperties.MailProperties(config);
    }
}
