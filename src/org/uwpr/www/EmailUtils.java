package org.uwpr.www;

import org.uwpr.AppProperties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtils
{

    public static void sendMail(String subject, String text, Address[] toAddress) throws MessagingException
    {
        sendMail(subject, text, toAddress, null);
    }

    public static void sendMail(String subject, String text, Address[] toAddresses, Address[] bccAddresses) throws MessagingException
    {
        AppProperties.MailProperties mailProps = AppProperties.getMailProps();
        if (mailProps == null)
        {
            throw new IllegalStateException("Mail properties were not initialized");
        }
        if (mailProps.getSmtpHost() == null)
        {
            throw new IllegalStateException("SMTP host was not found in the mail configuration.");
        }
        if (mailProps.getSenderEmail() == null)
        {
            throw new IllegalStateException("Sender email address was not found in the mail configuration.");
        }
        if (mailProps.getSenderPassword() == null)
        {
            throw new IllegalStateException("Sender email password was not found in the mail configuration.");
        }
        if ((toAddresses == null || toAddresses.length == 0) && (bccAddresses == null || bccAddresses.length == 0))
        {
            throw new IllegalStateException("No email addresses were found for sending the email.");
        }

        String smtpPort = mailProps.getSmtpPort();
        final String senderEmail = mailProps.getSenderEmail();
        final String senderPassword = mailProps.getSenderPassword();
        Authenticator auth = null;

        // set the SMTP host property value
        Properties properties = System.getProperties();
        String smtpHost = mailProps.getSmtpHost();
        properties.put("mail.smtp.host", smtpHost);

        if (smtpPort != null)
        {
            properties.put("mail.smtp.port", smtpPort);
        }

        if (senderPassword != null)
        {
            properties.put("mail.smtp.auth", "true"); //enable authentication
            auth = new Authenticator() {
                //override the getPasswordAuthentication method
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            };
        }

        // enable STARTTLS
        properties.put("mail.smtp.starttls.enable", "true");


        // create a JavaMail session
        javax.mail.Session mSession = javax.mail.Session.getInstance(properties, auth);

        // create a new MIME message
        MimeMessage message = new MimeMessage(mSession);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, toAddresses);

        if (bccAddresses != null && bccAddresses.length > 0)
        {
            Message.RecipientType type = toAddresses == null || toAddresses.length == 0 ? Message.RecipientType.TO : Message.RecipientType.BCC;
            message.setRecipients(type, bccAddresses);
        }

        message.setSubject(subject);
        message.setText(text);
        Transport.send(message);
    }
}
