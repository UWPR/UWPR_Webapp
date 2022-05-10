package org.uwpr.www.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uwpr.AdminUtils;
import org.uwpr.AppProperties;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.UsageBlockBase;
import org.uwpr.scheduler.UsageBlockBaseWithRate;
import org.uwpr.scheduler.UsageBlockPaymentInformation;
import org.uwpr.www.util.TimeUtils;
import org.yeastrc.project.Project;
import org.yeastrc.project.Researcher;
import org.yeastrc.project.payment.PaymentMethod;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by vsharma on 6/14/2016.
 */
public class ProjectInstrumentUsageUpdateEmailer
{
    private static ProjectInstrumentUsageUpdateEmailer instance = new ProjectInstrumentUsageUpdateEmailer();

    private static final Logger log = LogManager.getLogger(ProjectInstrumentUsageUpdateEmailer.class);

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

    public void sendEmail(Project project, MsInstrument instrument, Researcher user,
                          List<? extends UsageBlockBase> blocks,
                          UsageBlockPaymentInformation paymentInfo,
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
            StringBuilder usageDetails = new StringBuilder();
            usageDetails.append("UWPR Instrument usage ").append(action.name()).append("\n");
            if(actionDetailMessage != null)
            {
                usageDetails.append(actionDetailMessage).append("\n");
            }
            usageDetails.append("Instrument: ").append(instrument.getName()).append("\n");
            usageDetails.append("Project ID: " + project.getID() + "\n");
            usageDetails.append("Project title: " + project.getTitle() + "\n");
            usageDetails.append("User: " + user.getFullName() + "\n");
            usageDetails.append("User ID: " + user.getID() + "\n");

            usageDetails.append("Project URL: " + AppProperties.getHost() + "/pr/viewProject.do?ID="+project.getID()+"\n");

            if(paymentInfo != null)
            {
                for (int i = 0; i < paymentInfo.getCount(); i++)
                {

                    PaymentMethod pm = paymentInfo.getPaymentMethod(i);
                    BigDecimal perc = paymentInfo.getPercent(i);
                    usageDetails.append("Payment method ").append(perc.doubleValue() < 100.0 ? perc.toString() + "%: " : ": ")
                            .append(pm.getDisplayString()).append("\n");
                }
            }

            BigDecimal setupCost = BigDecimal.ZERO;
            BigDecimal signupCost = BigDecimal.ZERO;
            BigDecimal instrumentCost = BigDecimal.ZERO;
            boolean hasRate = false;

            StringBuilder usageBlockDetails = new StringBuilder();
            if(action.equals(Action.EDITED))
            {
                usageBlockDetails.append("New usage details: \n");
            }
            else if (action.equals(Action.DELETED))
            {
                usageBlockDetails.append("Deleted block details: \n");
            }
            else
            {
                usageBlockDetails.append("Usage details: \n");
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
                usageBlockDetails.append("Block ID: ").append(block.getID()).append(" ")
                        .append(TimeUtils.format(block.getStartDate())).append(" - ")
                        .append(TimeUtils.format(block.getEndDate()));

                if(operator != null)
                {
                    usageBlockDetails.append(", operator: ").append(operator);
                }

                if(block instanceof UsageBlockBaseWithRate)
                {
                    hasRate = true;
                    UsageBlockBaseWithRate blkWRate = (UsageBlockBaseWithRate) block;
                    BigDecimal blkSetupCost = blkWRate.getSetupCost();
                    BigDecimal blkSignupCost = blkWRate.getSignupCost();
                    BigDecimal blkInstrCost = blkWRate.getInstrumentCost();

                    setupCost = setupCost.add(blkSetupCost);
                    signupCost = signupCost.add(blkSignupCost);
                    instrumentCost = instrumentCost.add(blkInstrCost);

                    usageBlockDetails.append(", ")
                            .append("setup: $" + blkSetupCost)
                            .append(" signup: $" + blkSignupCost)
                            .append(" instrument: $").append(blkInstrCost);
                }

                usageBlockDetails.append("\n");
            }

            if(hasRate) {
                usageDetails.append("Setup cost: $").append(setupCost);
                usageDetails.append("\n");
                usageDetails.append("Signup cost: $").append(signupCost);
                usageDetails.append("\n");
                usageDetails.append("Instrument cost: $").append(instrumentCost);
                usageDetails.append("\n");
                usageDetails.append("Total cost: ").append(setupCost.add(signupCost.add(instrumentCost)));
                usageDetails.append("\n");
            }
            usageDetails.append("\n");

            usageDetails.append(usageBlockDetails);
            usageDetails.append("\n\nThank you,\nThe UW Proteomics Resource\n");

            System.out.println(usageDetails);
            message.setText(usageDetails.toString());

            // send the message
            Transport.send(message);

        } catch (Exception e) { log.error("Error sending email" , e); }

    }
}
