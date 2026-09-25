package org.uwpr.www;

import junit.framework.TestCase;
import org.uwpr.AppProperties;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.Properties;

/**
 * Guards the SMTP send against a mail server that never responds. A send with no timeouts held a
 * request thread for about 130 seconds when the configured host resolved to an address the server
 * could not reach, and the scheduler kept its progress dialog open for that whole time.
 */
public class EmailUtilsTest extends TestCase
{
    private static final String SMTP_HOST = "smtp.example.org";
    private static final String SENDER = "sender@example.org";
    private static final int TEST_TIMEOUT_MILLIS = 500;
    private static final long GIVE_UP_MILLIS = 10000;

    public final void testTimeoutsAreSet()
    {
        Properties properties = EmailUtils.buildMailProperties(mailProps(SMTP_HOST, "587"));

        assertEquals("A send should give up connecting after the configured timeout",
                String.valueOf(TEST_TIMEOUT_MILLIS), properties.get("mail.smtp.connectiontimeout"));
        assertEquals("A send should give up waiting for a response after the configured timeout",
                String.valueOf(TEST_TIMEOUT_MILLIS), properties.get("mail.smtp.timeout"));
        assertEquals("A send should give up writing after the configured timeout",
                String.valueOf(TEST_TIMEOUT_MILLIS), properties.get("mail.smtp.writetimeout"));
    }

    public final void testSystemPropertiesAreNotUsed()
    {
        EmailUtils.buildMailProperties(mailProps(SMTP_HOST, "587"));

        assertNull("Building the mail properties should not write mail.smtp.connectiontimeout into"
                + " the JVM system properties", System.getProperty("mail.smtp.connectiontimeout"));
        assertNull("Building the mail properties should not write mail.smtp.writetimeout into the"
                + " JVM system properties", System.getProperty("mail.smtp.writetimeout"));
    }

    public final void testSendGivesUpWhenServerNeverResponds() throws Exception
    {
        // Accepts the connection and never sends the SMTP greeting, so the send waits on the read.
        ServerSocket server = new ServerSocket(0);
        try
        {
            final AppProperties.MailProperties mailProps =
                    mailProps("127.0.0.1", String.valueOf(server.getLocalPort()));
            final Throwable[] thrown = new Throwable[1];

            Thread sender = new Thread(new Runnable() {
                public void run()
                {
                    try
                    {
                        EmailUtils.sendMail(mailProps, "subject", "text", recipients(), null);
                    }
                    catch (Throwable t)
                    {
                        thrown[0] = t;
                    }
                }
            });
            sender.setDaemon(true);
            long start = System.currentTimeMillis();
            sender.start();
            sender.join(GIVE_UP_MILLIS);
            long elapsed = System.currentTimeMillis() - start;

            assertFalse("A send to a mail server that never responds should return within "
                    + GIVE_UP_MILLIS + " ms", sender.isAlive());
            assertNotNull("A send to a mail server that never responds should throw", thrown[0]);
            assertEquals("A send to a mail server that never responds should fail on the read timeout",
                    SocketTimeoutException.class, rootCause(thrown[0]).getClass());
            assertTrue("A send should give up near the configured timeout, took " + elapsed + " ms",
                    elapsed < TEST_TIMEOUT_MILLIS * 10);
        }
        finally
        {
            server.close();
        }
    }

    private static Throwable rootCause(Throwable t)
    {
        while (t.getCause() != null)
        {
            t = t.getCause();
        }
        return t;
    }

    private static AppProperties.MailProperties mailProps(String host, String port)
    {
        return new AppProperties.MailProperties(host, port, SENDER, "password", TEST_TIMEOUT_MILLIS);
    }

    private static Address[] recipients() throws Exception
    {
        return new Address[] { new InternetAddress("recipient@example.org") };
    }
}
