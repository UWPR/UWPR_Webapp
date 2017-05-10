package org.uwpr.www.scheduler;

import org.apache.log4j.Logger;
import org.uwpr.AdminUtils;
import org.uwpr.AppProperties;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.UsageBlockBase;
import org.uwpr.www.util.TimeUtils;
import org.yeastrc.project.Project;
import org.yeastrc.project.Researcher;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * Created by vsharma on 6/14/2016.
 */
public class ProjectInstrumentUsageUpdateEmailer
{
    private static ProjectInstrumentUsageUpdateEmailer instance = new ProjectInstrumentUsageUpdateEmailer();

    private static final Logger log = Logger.getLogger(ProjectInstrumentUsageUpdateEmailer.class);

    public enum Action
    {
        ADDED,
        EDITED,
        DELETED,
        PURGED;
    }

    private ProjectInstrumentUsageUpdateEmailer() {}

    public static ProjectInstrumentUsageUpdateEmailer getInstance() {
        return instance;
    }

    public void sendEmail(Project project, MsInstrument instrument, Researcher user, List<? extends UsageBlockBase> blocks,
                          Action action, String actionDetailMessage,
                          boolean includeProjectResearchers) {

        log.info("Sending project instrument update email");

        List<Researcher> admins = AdminUtils.getNotifyAdmins();

        try {
            // set the SMTP host property value
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", "localhost");

            // create a JavaMail session
            javax.mail.Session mSession = javax.mail.Session.getInstance(properties, null);

            // create a new MIME message
            MimeMessage message = new MimeMessage(mSession);

            // set the from address
            Address fromAddress = AppProperties.getFromAddress();
            message.setFrom(fromAddress);

            // set the bcc address
            String emailStr = "";
            for(Researcher r: admins) {
                emailStr += ","+r.getEmail();
            }
            if(emailStr.length() > 0) emailStr = emailStr.substring(1); // remove first comma

            Address[] bccAddress = InternetAddress.parse(emailStr);
            message.setRecipients(Message.RecipientType.BCC, bccAddress);

            if(includeProjectResearchers)
            {
                List<Researcher> projectResearchers = project.getResearchers();
                emailStr = "";
                emailStr += project.getPI().getEmail();
                for (Researcher r : projectResearchers) {
                    emailStr += "," + r.getEmail();
                }
                Address[] toAddress = InternetAddress.parse(emailStr);
                message.setRecipients(Message.RecipientType.TO, toAddress);
            }

            // set the subject
            StringBuilder subject = new StringBuilder("UWPR - Instrument usage ");
            subject.append(action);
            subject.append(" on ").append(instrument.getName());

            message.setSubject(subject.toString());

            // set the message body
            StringBuilder text = new StringBuilder();
            text.append("UWPR Instrument usage ").append(action.name()).append("\n");
            if(actionDetailMessage != null)
            {
                text.append(actionDetailMessage).append("\n");
            }
            text.append("Instrument: ").append(instrument.getName()).append("\n");
            text.append("Project ID: " + project.getID() + "\n");
            text.append("Project title: " + project.getTitle() + "\n");
            text.append("User: " + user.getFullName() + "\n");
            text.append("User ID: " + user.getID() + "\n");

            text.append("Project URL: " + AppProperties.getHost() + "/pr/viewProject.do?ID="+project.getID()+"\n");

            text.append("\n");
            if(action.equals(Action.EDITED))
            {
                text.append("New usage details: \n");
            }
            else {
                text.append("Usage details: \n");
            }
            Map<Integer, String> userIdMap = new HashMap<Integer, String>();
            for(UsageBlockBase block: blocks)
            {
                String operator = userIdMap.get(block.getInstrumentOperatorId());
                if(operator == null)
                {
                    Researcher instrumentOperator = new Researcher();
                    instrumentOperator.load(block.getInstrumentOperatorId());
                    operator = instrumentOperator.getFullName();
                    userIdMap.put(block.getInstrumentOperatorId(), operator);
                }
                text.append("Block ID: ").append(block.getID()).append(" ")
                        .append(TimeUtils.format(block.getStartDate())).append(" - ")
                        .append(TimeUtils.format(block.getEndDate()));
                if(operator != null)
                {
                    text.append(", operator: ").append(operator);
                }
                text.append("\n");
            }

            text.append("\n\nThank you,\nThe UW Proteomics Resource\n");

            System.out.println(text);
            message.setText(text.toString());

            // send the message
            Transport.send(message);

        } catch (Exception e) { log.error("Error sending email" , e); }

    }
}
