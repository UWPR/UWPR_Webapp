/**
 * CollaborationStatusChangeEmailer.java
 * @author Vagisha Sharma
 * Dec 18, 2008
 * @version 1.0
 */
package org.yeastrc.www.project;

import org.apache.log4j.Logger;
import org.yeastrc.project.*;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 
 */
public class CollaborationStatusChangeEmailer {

    private static final Logger log = Logger.getLogger(CollaborationStatusChangeEmailer.class);
    
    private CollaborationStatusChangeEmailer() {}
    
    public static void sendCollaborationAcceptedEmail(Collaboration collaboration) {
        log.info("Sending Project Accepted email for project: "+collaboration.getID());
        
        StringBuilder buf = new StringBuilder();
        buf.append("\n\n");
        buf.append("The project listed below has been accepted by the UWPR committee.\n"); 
        
        // Add any comments that are to be sent to the investigator(s)
        buf.append(getEmailComments(collaboration));
        
        buf.append("All researchers collaborating with the UWPR are required to submit a project summary to the UWPR advisory committee within 12 months. ");
        buf.append("Any further use of the facility after 12 month will require approval of a new abstract by the governing committee\n");
        
        buf.append("\n\n");
        buf.append("Please acknowledge the \"University of Washington's Proteomics Resource (UWPR95794)\" in all publications."); 
        buf.append("\n\n");
        buf.append("Contact Priska von Haller (priska@u.washington.edu) to schedule instrument time or Jimmy Eng (engj@u.washington.edu) for data analysis."); 
        
        buf.append("\n\n");
        buf.append("Thank you for collaborating with the UWPR.\n"); 
        buf.append("\n\n\n");

        buf.append(getProjectDetails(collaboration));
        buf.append("\n");

//        System.out.println(buf.toString());
        
        sendEmailToResearchers(collaboration, buf.toString(), "UWPR collaboration request (ID: "+collaboration.getID()+") accepted");
    }

    private static String getEmailComments(Collaboration collaboration) {
        
        List<String> reviewerComments = null;
        try {
            reviewerComments = getReviewerComments(collaboration.getID());
        }
        catch(SQLException ex) {
            log.error("COULD NOT GET REVIEWERS' COMMENTS TO INCLUDE IN EMAIL");
            return "";
        }
        // Add any comments that are to be included in the email
        StringBuilder buf = new StringBuilder();
        if(reviewerComments != null && reviewerComments.size() > 0) {
            buf.append("\nReviewers' Comments: \n\n");
            int idx = 1;
            for(String comments: reviewerComments) {
                buf.append("Reviewer "+idx+++": \n");
                buf.append(comments+"\n\n");
            }
            buf.append("\n");
        }
        return buf.toString();
    }
    
    private static List<String> getReviewerComments(int projectId) throws SQLException {
        ProjectReviewerDAO prDao = ProjectReviewerDAO.instance();
        List<ProjectReviewer> reviewers = prDao.getProjectReviewers(projectId);
        
        List<String> comments = new ArrayList<String>(reviewers.size());
        for(ProjectReviewer reviewer: reviewers) {
            if(reviewer.getEmailComment() != null && reviewer.getEmailComment().trim().length() > 0)
                comments.add(reviewer.getEmailComment());
        }
        
        return comments;
    }
    
    public static void sendCollaborationRejectedEmail(Collaboration collaboration) {
        log.info("Sending Project Rejected email for project: "+collaboration.getID());
        
        List<CollaborationRejectionCause> causeList = RejectionCauseDAO.instance().findProjectRejectionCauseList(collaboration.getID());
        
        StringBuilder buf = new StringBuilder();
        buf.append("\n\n");
        buf.append("The project listed below has been rejected by the UWPR committee for the following reasons:\n");
        for(CollaborationRejectionCause cause: causeList) {
            buf.append("    -- "+cause.getDescription()+"\n");
        }
        
        // Add any comments that are to be sent to the investigator(s)
        String emailComments = getEmailComments(collaboration);
        buf.append(emailComments);
        
        if(emailComments.length() == 0)
            buf.append("\n\n");
        buf.append(getProjectDetails(collaboration));
        buf.append("\n");
//        System.out.println(buf.toString());
        sendEmailToResearchers(collaboration, buf.toString(), "UWPR collaboration request (ID: "+collaboration.getID()+") rejected");
    }
    
    public static void sendRevisionRequestedEmail(Collaboration collaboration) {
        log.info("Sending revision request email for project: "+collaboration.getID());
        
        StringBuilder buf = new StringBuilder();
        buf.append("\n\n");
        buf.append("The UWPR committee requires additional information in order to review your collaboration request. ");
        buf.append("Please see the reviewers' comments below:");
        buf.append("\n\n");
        // Add comments that are to be sent to the investigator(s)
        buf.append(getEmailComments(collaboration));
        buf.append("\n");
        buf.append("You may use the \"Comments\" field to address the reviewers' concerns. ");
        buf.append("When you save the revised abstract, please check the \"Notify Reviewers\" check-box.\n");
        buf.append(getProjectDetails(collaboration));
        buf.append("\n");
        buf.append("\n\n");
        //System.out.println(buf.toString());
        sendEmailToResearchers(collaboration, buf.toString(), "UWPR collaboration request (ID: "+collaboration.getID()+") -- INFORMATION REQUESTED");
    }
    
    public static void sendCollaborationCompletedEmail(Collaboration collaboration) {
        log.info("Sending Project Completed email for project: "+collaboration.getID());
        StringBuilder buf = new StringBuilder();
        buf.append("\n\n");
        buf.append("The project listed below has been completed. The UWPR is now operated as a cost center. ");
        buf.append("Projects submitted as billable collaborations will no longer require committee approval. ");
        buf.append("Please check our home page for more information.\n\n");
        buf.append("Please submit a project summary to the UWPR advisory committee within two months. ");
        buf.append("This project summary should highlight scientific progress that was enabled by the UWPR collaboration. ");
        buf.append("You can submit the report via the UWPR Informatics Platform (https://proteomicsresource.washington.edu/pr/pages/login/login.jsp). ");
        buf.append("Navigate to your project page and click on \"Edit Project\".  ");
        buf.append("Enter your report in the \"Progress\" field and use the \"Publications\" field to list any publications resulting from the collaboration.");
        buf.append("\n\n");
        buf.append("Please acknowledge the \"University of Washington's Proteomics Resource (UWPR95794)\" in all publications.");
        buf.append("\n\n");
        buf.append("If you wish to continue this collaboration you can request a collaboration extension. ");
        buf.append("After submitting a progress report (see above) you will be able to request a collaboration extension by clicking on Extend as Billed Project. ");
        buf.append("\n\n");
        buf.append("Thank you for collaborating with the UWPR."); 
        
        buf.append("\n\n\n");
        buf.append(getProjectDetails(collaboration));
        buf.append("\n");

        sendEmailToResearchers(collaboration, buf.toString(), "UWPR Project (ID: "+collaboration.getID()+") completed");
    }
    
    public static void sendCollaborationExpiredEmail(Collaboration collaboration) {
        log.info("Sending Project Expired email for project: "+collaboration.getID());
        StringBuilder buf = new StringBuilder();
        buf.append("\n\n");
        buf.append("The project listed below has expired.\n"); 
        buf.append("In case you haven't already done so, please submit a project summary to the UWPR advisory committee. ");
        buf.append("This project summary should highlight scientific progress that was enabled by the UWPR collaboration. ");
        buf.append("You can submit the report via the UWPR Informatics Platform (https://proteomicsresource.washington.edu/pr/pages/login/login.jsp). ");
        buf.append("Navigate to your project page and click on \"Edit Project\".  ");
        buf.append("Enter your report in the \"Progress\" field and use the \"Publications\" field to list any publications resulting from the collaboration.");
        buf.append("\n\n");
        buf.append("Please acknowledge the \"University of Washington's Proteomics Resource (UWPR95794)\" in all publications."); 
        buf.append("\n\n");
        buf.append("Any further use of the facility will require approval of a new abstract by the governing committee.");

        buf.append("\n\n");
        
        buf.append("If you wish to continue this collaboration you can request a collaboration extension. ");
        buf.append("After submitting a progress report (see above) you will be able to request a collaboration extension by clicking on Extend Project. ");
        buf.append("The appropriate members of the governing UWPR committee will be notified to review your project extension request");
        buf.append("\n\n");
        
        buf.append("Thank you for collaborating with the UWPR."); 
        buf.append("\n\n\n");

        buf.append(getProjectDetails(collaboration));
        buf.append("\n");

        sendEmailToResearchers(collaboration, buf.toString(), "UWPR Project (ID: "+collaboration.getID()+") expired");
    }
    
    public static void sendReportReminderEmail(Collaboration collaboration) {
        log.info("Sending Report due reminder for project: "+collaboration.getID());
        StringBuilder buf = new StringBuilder();
        buf.append("\n\n");
        buf.append("The project listed below is either complete or will expire in two months.\n"); 
        buf.append("Please submit a project summary to the UWPR advisory committee within two months. ");
        buf.append("This project summary should highlight scientific progress that was enabled by the UWPR collaboration. ");
        buf.append("You can submit the report via the UWPR Informatics Platform (https://proteomicsresource.washington.edu/pr/pages/login/login.jsp). ");
        buf.append("Navigate to your project page and click on \"Edit Project\".  ");
        buf.append("\n\n");
        buf.append("Please acknowledge the \"University of Washington's Proteomics Resource (UWPR95794)\" in all publications."); 
        buf.append("\n\n");
        buf.append("Any further use of the facility will require approval of a new abstract by the governing committee.");

        
        buf.append("If you wish to continue this collaboration you can request a collaboration extension. ");
        buf.append("After submitting a progress report (see above) you will be able to request a collaboration extension by clicking on Extend Project. ");
        buf.append("The appropriate members of the governing UWPR committee will be notified to review your project extension request");
        buf.append("\n\n");
        
        buf.append("\n\n");
        buf.append("Thank you for collaborating with the UWPR."); 
        buf.append("\n\n\n");

        buf.append(getProjectDetails(collaboration));
        buf.append("\n");

        sendEmailToResearchers(collaboration, buf.toString(), "UWPR Project (ID: "+collaboration.getID()+") progress report reminder");
    }
    
    private static void sendEmailToResearchers(Collaboration collaboration, String text, String subject) {
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
            Address[] toAddress = InternetAddress.parse(collaboration.getPI().getEmail());
            message.setRecipients(Message.RecipientType.TO, toAddress);
            printEmailAddresses(toAddress);
            
            // Are there other researchers on this project? If so, CC them
            // set the cc address by assembling a comma delimited list of addresses associated with the groups selected
            String emailStr = "";
            for(Researcher researcher: collaboration.getResearchers())
            {
                emailStr += "," + researcher.getEmail();
            }
            
            if(emailStr.length() > 0) {
                emailStr = emailStr.substring(1, emailStr.length());
                Address[] ccAddresses = InternetAddress.parse(emailStr);
                message.setRecipients(Message.RecipientType.CC, ccAddresses);
                printEmailAddresses(ccAddresses);
            }

            // set the BCC address (Priska)
            Researcher priska = new Researcher();
            priska.load(1756);
            Researcher vsharma = new Researcher();
            vsharma.load(1811);
            String bccEmail = priska.getEmail()+","+vsharma.getEmail();
             
            Address[] bccAddresses = InternetAddress.parse(bccEmail);
            printEmailAddresses(bccAddresses);
            message.setRecipients(Message.RecipientType.BCC, bccAddresses);
            
            // set the subject
            message.setSubject(subject);
            
            // set the message body
            message.setText(text);

            // send the message
            Transport.send(message);

        } catch (Exception e) { log.error("Error sending email.", e); }
    }
    
    private static void printEmailAddresses(Address[] addresses) {
        if(addresses == null)
            return;
        for(Address a: addresses) {
            if(a != null)
                log.info("\tAddress: "+a.toString());
        }
    }
    
    private static String getProjectDetails(Collaboration collaboration) {
        StringBuilder buf = new StringBuilder();
        buf.append("Project Details:\n");
        buf.append("Project ID: "+collaboration.getID());
        buf.append("\n");
        buf.append("Title: "+collaboration.getTitle());
        buf.append("\n");
        Researcher pi = collaboration.getPI();
        buf.append("Lab Director: "+pi.getFirstName()+" "+pi.getLastName()+", "+pi.getOrganization());
        return buf.toString();
    }
    
}
