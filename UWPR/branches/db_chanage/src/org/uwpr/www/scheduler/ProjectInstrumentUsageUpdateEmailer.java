package org.uwpr.www.scheduler;

import org.apache.log4j.Logger;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.UsageBlockBase;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.Project;
import org.yeastrc.project.Researcher;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
        DELETED;
    }

    private static SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
    private ProjectInstrumentUsageUpdateEmailer() {}

    public static ProjectInstrumentUsageUpdateEmailer getInstance() {
        return instance;
    }

    public void sendEmail(Project project, MsInstrument instrument, Researcher user, List<? extends UsageBlockBase> blocks, Action action) {

        log.info("Sending project instrument update email");

        List<Researcher> researchers = getAdmins();

        try {
            // set the SMTP host property value
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", "localhost");

            // create a JavaMail session
            javax.mail.Session mSession = javax.mail.Session.getInstance(properties, null);

            // create a new MIME message
            MimeMessage message = new MimeMessage(mSession);

            // set the from address
            Address fromAddress = new InternetAddress("do_not_reply@proteomicsresource.washington.edu");
            message.setFrom(fromAddress);

            // set the to address
            String emailStr = "";
            for(Researcher r: researchers) {
                emailStr += ","+r.getEmail();
            }
            if(emailStr.length() > 0) emailStr = emailStr.substring(1); // remove first comma

            Address[] toAddress = InternetAddress.parse(emailStr);
            message.setRecipients(Message.RecipientType.TO, toAddress);


            // set the subject
            StringBuilder subject = new StringBuilder("UWPR - Instrument usage ");
            subject.append(action);
            subject.append(" on ").append(instrument.getName());

            message.setSubject(subject.toString());

            // set the message body
            StringBuilder text = new StringBuilder();
            text.append("Instrument usage ").append(action.name().toLowerCase()).append("\n");
            text.append("Instrument: ").append(instrument.getName()).append("\n");
            text.append("Project ID: " + project.getID() + "\n");
            text.append("Project title: " + project.getTitle() + "\n");
            text.append("User: " + user.getFullName() + "\n");
            text.append("User ID: " + user.getID() + "\n");

            text.append("Calendar URL: http://proteomicsresource.washington.edu/pr/viewAllInstrumentCalendar.do \n");
            text.append("Project URL: http://proteomicsresource.washington.edu/pr/viewProject.do?ID="+project.getID()+"\n");

            text.append("\n");
            text.append("Details: \n");
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
                text.append(block.getID()).append(" ")
                        .append(format.format(block.getStartDate())).append(" - ")
                        .append(format.format(block.getEndDate()));
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

    private List<Researcher> getAdmins()
    {
        // TODO Cannot use hardcoded database IDs
        Researcher priska = new Researcher();
        try {
            priska.load(1756);
        } catch (InvalidIDException e) {
            log.error("No researcher found for ID: 1756", e);
        } catch (SQLException e) {
            log.error("Error loading reseracher for ID: 1756", e);
        }
        Researcher vsharma = new Researcher();
        try {
            vsharma.load(1811);
        } catch (InvalidIDException e) {
            log.error("No researcher found for ID: 1811", e);
        } catch (SQLException e) {
            log.error("Error loading reseracher for ID: 1811", e);
        }

        Researcher engj = new Researcher();
        try {
            engj.load(1752);
        } catch (InvalidIDException e) {
            log.error("No researcher found for ID: 1752", e);
        } catch (SQLException e) {
            log.error("Error loading reseracher for ID: 1752", e);
        }

        List<Researcher> researchers = new ArrayList<Researcher>(2);
        researchers.add(priska);
        researchers.add(vsharma);
        researchers.add(engj);
        return researchers;
    }
}
