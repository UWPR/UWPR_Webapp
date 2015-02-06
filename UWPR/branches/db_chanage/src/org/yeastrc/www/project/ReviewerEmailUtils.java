/**
 * 
 */
package org.yeastrc.www.project;

import org.apache.log4j.Logger;
import org.yeastrc.data.InvalidIDException;
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
 * ReviewerEmailUtils.java
 * @author Vagisha Sharma
 * Jun 23, 2010
 * 
 */
public class ReviewerEmailUtils {

	private static final Logger log = Logger.getLogger(ReviewerEmailUtils.class);

	private ReviewerEmailUtils(){}

	public static void emailReviewer(Collaboration collaboration) 
	throws InvalidIDException, SQLException {


		ProjectReviewerDAO prDao = ProjectReviewerDAO.instance();
		List<ProjectReviewer> reviewers = prDao.getProjectReviewers(collaboration.getID());

		List<Researcher> researchers = new ArrayList<Researcher>(reviewers.size());
		for(ProjectReviewer r: reviewers) {
			researchers.add(r.getResearcher());
		}

		String reviewerNames = "";
		for(Researcher r: researchers) 
			reviewerNames += r.getFirstName()+" "+r.getLastName()+"\n";

		log.info("Emailing reviewers: "+reviewerNames+"; for project: "+collaboration.getID());

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

			// set the CC address (Priska)
			Researcher priska = new Researcher();
			priska.load(1756);
			Researcher vsharma = new Researcher();
			vsharma.load(1811);
			String ccEmail = priska.getEmail()+","+vsharma.getEmail();

			Address[] ccAddress = InternetAddress.parse(ccEmail);
			message.setRecipients(Message.RecipientType.CC, ccAddress);

			// set the subject
			if(collaboration.getParentProjectID() > 0)
				message.setSubject("UWPR - Review Collaboration Extension Request #" + collaboration.getID());
			else
				message.setSubject("UWPR - Review Collaboration Request #" + collaboration.getID());

			// set the message body
			String text = reviewerNames;
			text += "\n\n";
			text += "You have been assigned to review the following collaboration request for the next UWPR committee meeting.\n\n";
			text += "NOTE: free instrument time is intended for small scale projects (max 50 RAW files or 5 days). ";
			text += "Please make sure the collaborators have a good explanation as to why they cannot pay for instrument time (e.g. 24hrs of orbitrap time = $ 270.0).\n\n";
			text += "You can either review the project online or email your recommendation to Priska von Haller ("+priska.getEmail()+").\n\n";
			text += getCollaborationDetails(collaboration);

			text += "\n\nThank you,\nThe UW Proteomics Resource\n";

			//System.out.println(text);
			message.setText(text);

			// send the message
			Transport.send(message);

		} catch (Exception e) { log.error("Error sending email to assigned reviewers" , e); }
	}

	public static void sendAbstractUpdateEmail(Collaboration collaboration) 
	throws InvalidIDException, SQLException {


		ProjectReviewerDAO prDao = ProjectReviewerDAO.instance();
		List<ProjectReviewer> reviewers = prDao.getProjectReviewers(collaboration.getID());

		List<Researcher> researchers = new ArrayList<Researcher>(reviewers.size());
		for(ProjectReviewer r: reviewers) {
			researchers.add(r.getResearcher());
		}

		String reviewerNames = "";
		for(Researcher r: researchers) 
			reviewerNames += r.getFirstName()+" "+r.getLastName()+"\n";

		log.info("Abstract update email to reveiwers: "+reviewerNames+"; for project: "+collaboration.getID());

		try {
			//set the SMTP host property value
			Properties properties = System.getProperties();
			properties.put("mail.smtp.host", "localhost");

			//create a JavaMail session
			javax.mail.Session mSession = javax.mail.Session.getInstance(properties, null);

			//create a new MIME message
			MimeMessage message = new MimeMessage(mSession);

			//set the from address
			Address fromAddress = new InternetAddress("do_not_reply@proteomicsresource.washington.edu");
			message.setFrom(fromAddress);

			//set the to address
			String emailStr = "";
			for(Researcher r: researchers) {
				emailStr += ","+r.getEmail();
			}
			if(emailStr.length() > 0) emailStr = emailStr.substring(1); // remove first comma

			Address[] toAddress = InternetAddress.parse(emailStr);
			message.setRecipients(Message.RecipientType.TO, toAddress);

			//set the CC address (Priska)
			Researcher priska = new Researcher();
			priska.load(1756);
			Researcher vsharma = new Researcher();
			vsharma.load(1811);
			String ccEmail = priska.getEmail()+","+vsharma.getEmail();

			Address[] ccAddress = InternetAddress.parse(ccEmail);
			message.setRecipients(Message.RecipientType.CC, ccAddress);

			//set the subject
			message.setSubject("UWPR - Collaboration Request #" + collaboration.getID()+" abstract UPDATED");

			//set the message body
			String text = reviewerNames;
			text += "\n\n";
			text += "Abstract details for the following collaboration request have been updated.\n\n";
			text += "You can either review the project online or email your recommendation to Priska von Haller ("+priska.getEmail()+").\n\n";
			text += getCollaborationDetails(collaboration);

			text += "\n\nThank you,\nThe UW Proteomics Resource\n";

			// System.out.println(text);
			message.setText(text);

			//send the message
			Transport.send(message);

		} catch (Exception e) { log.error("Error sending abstract updated email to assigned reviewers" , e); }
	}

	public static void sendReviewReminderEmail(Collaboration collaboration, ProjectReviewer reviewer) {

		Researcher researcher = reviewer.getResearcher();

		String reviewerName = researcher.getFirstName()+" "+researcher.getLastName()+"\n";

		log.info("Sending reminder email to reviewer: "+reviewerName+"; for project: "+collaboration.getID());

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
			String emailStr =  researcher.getEmail();
			log.info("Reviewer email address: "+emailStr);

			Address[] toAddress = InternetAddress.parse(emailStr);
			message.setRecipients(Message.RecipientType.TO, toAddress);

			// set the CC address (Priska)
			Researcher priska = new Researcher();
			priska.load(1756);
			Researcher vsharma = new Researcher();
			vsharma.load(1811);
			String ccEmail = priska.getEmail()+","+vsharma.getEmail();

			Address[] ccAddress = InternetAddress.parse(ccEmail);
			message.setRecipients(Message.RecipientType.CC, ccAddress);

			// set the subject
			message.setSubject("REMINDER: UWPR - Review Collaboration Request #" + collaboration.getID());

			// set the message body
			String text = "";
			text += "The following collaboration request is pending your review.\n\n";
			text += "NOTE: free instrument time is intended for small scale projects (max 50 RAW files or 5 days). ";
			text += "Please make sure the collaborators have a good explanation as to why they cannot pay for instrument time (e.g. 24hrs of orbitrap time = $ 270.0).\n\n";
			text += "You can either review the project online or email your recommendation to Priska von Haller ("+priska.getEmail()+").\n\n";
			text += getCollaborationDetails(collaboration);

			text += "\n\nThank you,\nThe UW Proteomics Resource\n";

			// System.out.println(text);
			message.setText(text);

			// send the message
			Transport.send(message);

		} catch (Exception e) { log.error("Error sending reminder email to reviewer" , e); }
	}


	public static void sendReviewConflictEmail(Collaboration collaboration, List<ProjectReviewer> reviewers) {


		log.info("Sending review conflict email to reviewers for project: "+collaboration.getID());
		for(ProjectReviewer reviewer: reviewers) {
			log.info("\tReviewer: "+reviewer.getResearcher().getEmail());
		}

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
			for(ProjectReviewer reviewer: reviewers) {
				emailStr += "," + reviewer.getResearcher().getEmail();
			}
			if(emailStr.length() > 0)
				emailStr = emailStr.substring(1);

			log.info("Reviewer email addresses: "+emailStr);

			Address[] toAddress = InternetAddress.parse(emailStr);
			message.setRecipients(Message.RecipientType.TO, toAddress);

			// set the CC address (Priska)
			Researcher priska = new Researcher();
			priska.load(1756);
			Researcher vsharma = new Researcher();
			vsharma.load(1811);
			String ccEmail = priska.getEmail()+","+vsharma.getEmail();

			Address[] ccAddress = InternetAddress.parse(ccEmail);
			message.setRecipients(Message.RecipientType.CC, ccAddress);

			// set the subject
			message.setSubject("REVIEW CONFLICT: UWPR - Collaboration Request #" + collaboration.getID());

			// set the message body
			String text = "";
			text += "There was a conflict in the reviews for the following collaboration request.\n\n";

			// get the recommendation and comments for each reviewer
			for(ProjectReviewer reviewer: reviewers) {
				text += "Reviewer: "+reviewer.getResearcher().getFirstName()+" "+reviewer.getResearcher().getLastName()+"\n";
				text += "Recommended Status: "+reviewer.getRecommendedStatus().getLongName()+"\n";
				if(reviewer.getRecommendedStatus() == CollaborationStatus.REJECTED)
					text += "Rejection Causes(s): "+reviewer.getRejectionCauseString()+"\n";
				text += "Comments: \n";
				if(reviewer.getComment() != null && reviewer.getComment().trim().length() > 0)
					text += reviewer.getComment()+"\n";
				if(reviewer.getEmailComment() != null && reviewer.getEmailComment().trim().length() > 0)
					text += reviewer.getEmailComment()+"\n";
				text += "\n\n";
			}

			text += getCollaborationDetails(collaboration);

			text += "\n\nThank you,\nThe UW Proteomics Resource\n";

			//System.out.println(text);
			message.setText(text);

			// send the message
			Transport.send(message);

		} catch (Exception e) { log.error("Error sending review conflict email" , e); }
	}

	public static void sendReviewConflictReminderEmail(Collaboration collaboration, List<ProjectReviewer> reviewers) {


		log.info("Sending review conflict REMINDER email to reviewers for project: "+collaboration.getID());
		for(ProjectReviewer reviewer: reviewers) {
			log.info("\tReviewer: "+reviewer.getResearcher().getEmail());
		}

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
			for(ProjectReviewer reviewer: reviewers) {
				emailStr += "," + reviewer.getResearcher().getEmail();
			}
			if(emailStr.length() > 0)
				emailStr = emailStr.substring(1);

			log.info("Reviewer email addresses: "+emailStr);

			Address[] toAddress = InternetAddress.parse(emailStr);
			message.setRecipients(Message.RecipientType.TO, toAddress);

			// set the CC address (Priska)
			Researcher priska = new Researcher();
			priska.load(1756);
			Researcher vsharma = new Researcher();
			vsharma.load(1811);
			String ccEmail = priska.getEmail()+","+vsharma.getEmail();

			Address[] ccAddress = InternetAddress.parse(ccEmail);
			message.setRecipients(Message.RecipientType.CC, ccAddress);

			// set the subject
			message.setSubject("REVIEW CONFLICT REMINDER: UWPR - Collaboration Request #" + collaboration.getID());

			// set the message body
			String text = "";
			text += "There was a conflict in the reviews for the following collaboration request.\n\n";

			// get the recommendation and comments for each reviewer
			for(ProjectReviewer reviewer: reviewers) {
				text += "Reviewer: "+reviewer.getResearcher().getFirstName()+" "+reviewer.getResearcher().getLastName()+"\n";
				text += "Recommended Status: "+reviewer.getRecommendedStatus().getLongName()+"\n";
				if(reviewer.getRecommendedStatus() == CollaborationStatus.REJECTED)
					text += "Rejection Causes(s): "+reviewer.getRejectionCauseString()+"\n";
				text += "Comments: \n";
				if(reviewer.getComment() != null && reviewer.getComment().trim().length() > 0)
					text += reviewer.getComment()+"\n";
				if(reviewer.getEmailComment() != null && reviewer.getEmailComment().trim().length() > 0)
					text += reviewer.getEmailComment()+"\n";
				text += "\n\n";
			}

			text += getCollaborationDetails(collaboration);

			text += "\n\nThank you,\nThe UW Proteomics Resource\n";

			//System.out.println(text);
			message.setText(text);

			// send the message
			Transport.send(message);

		} catch (Exception e) { log.error("Error sending review conflict reminder email" , e); }
	}

	private static String getCollaborationDetails(Collaboration collaboration) {
		
		StringBuilder buf = new StringBuilder();
		
		if(collaboration.getParentProjectID() > 0) {
			buf.append("NOTE: This is an extension project.\n");
		}
		buf.append("Project Title: "+collaboration.getTitle()+"\n");
		buf.append("Project URL: http://proteomicsresource.washington.edu/pr/viewProject.do?ID="+collaboration.getID()+"\n\n");
		buf.append("PI: "+collaboration.getPI().getFirstName()+" "+collaboration.getPI().getLastName()+
		", "+collaboration.getPI().getDegree()+"\n");

        for(Researcher researcher: collaboration.getResearchers())
        {
            buf.append("Researcher: ").append(researcher.getFirstName()).append(" ").append(researcher.getLastName()).append(", ").append(researcher.getDegree()).append("\n");
        }


		buf.append("\nCollaborating with: "+collaboration.getGroupsString()+"\n");

		if(collaboration.getScientificQuestion() != null && collaboration.getScientificQuestion().trim().length() > 0) {
			buf.append("\nScientific Question: "+collaboration.getScientificQuestion()+"\n");
		}

		buf.append("\nProject Abstract: \n");
		buf.append(collaboration.getAbstract());
		buf.append("\n");

		if(collaboration.getComments() != null && collaboration.getComments().trim().length() > 0) {
			buf.append( "\nComments: "+collaboration.getComments()+"\n");
		}

		if(collaboration.getParentProjectID() > 0) {
			buf.append( "\nParent Project: "+collaboration.getParentProjectID()+"\n");
			buf.append( "Parent Project URL: http://proteomicsresource.washington.edu/pr/viewProject.do?ID="+collaboration.getParentProjectID()+"\n");
			buf.append( "Extension Reasons: \n");
			buf.append( collaboration.getExtensionReasons()+"\n");
		}


		buf.append( "\n#Runs requested:\n");
		buf.append( "LTQ: "+collaboration.getLtqRunsRequested()+"\n");
		buf.append( "LTQ-FT: "+collaboration.getLtq_ftRunsRequested()+"\n");
		buf.append( "LTQ-ETD: "+collaboration.getLtq_etdRunsRequested()+"\n");
		buf.append( "LTQ-Orbitrap: "+collaboration.getLtq_orbitrapRunsRequested()+"\n");
		buf.append( "TSQ-Access: "+collaboration.getTsq_accessRunsRequested()+"\n");
		buf.append( "TSQ-Vantage: "+collaboration.getTsq_vantageRunsRequested()+"\n");

		buf.append("\nInstrument time justification:\n");
		buf.append(collaboration.getInstrumentTimeExpl()+"\n");
		
		
		String fragType = collaboration.getFragmentationTypesString();
		if(fragType != null && fragType.trim().length() > 0) {
			buf.append( "\nFragmentation types: "+fragType+"\n");
		}

		buf.append("\nMass Spec. analysis by UWPR personnel: ");
		if(collaboration.isMassSpecExpertiseRequested())
			buf.append( " Yes\n");
		else
			buf.append( " No\n");

		buf.append("Database search performed at UWPR: ");
		if(collaboration.isDatabaseSearchRequested())
			buf.append( " Yes\n");
		else
			buf.append( " No\n");
		return buf.toString();
	}

	public static void emailVsharma(Collaboration collaboration, ProjectReviewer reviewer) {


		log.info("Sending email to vsharma. Review submitted for project: "+collaboration.getID());

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

			Researcher vsharma = new Researcher();
			vsharma.load(1811);

			// set the to address
			String emailStr = vsharma.getEmail();
			log.info("Sending email to: "+emailStr);

			Address[] toAddress = InternetAddress.parse(emailStr);
			message.setRecipients(Message.RecipientType.TO, toAddress);

			// set the subject
			message.setSubject("REVIEW SUBMITTED: UWPR - Collaboration Request #" + collaboration.getID());

			// set the message body
			String text = "";
			text += "A review has been submitted for project: "+collaboration.getID()+".\n\n";

			// get the recommendation and comments for each reviewer
			text += "Reviewer: "+reviewer.getResearcher().getFirstName()+" "+reviewer.getResearcher().getLastName()+"\n";
			text += "Recommended Status: "+reviewer.getRecommendedStatus().getLongName()+"\n";
			if(reviewer.getRecommendedStatus() == CollaborationStatus.REJECTED)
				text += "Rejection Causes(s): "+reviewer.getRejectionCauseString()+"\n";
			text += "Comments: \n";
			if(reviewer.getComment() != null && reviewer.getComment().trim().length() > 0)
				text += reviewer.getComment()+"\n";
			if(reviewer.getEmailComment() != null && reviewer.getEmailComment().trim().length() > 0)
				text += reviewer.getEmailComment()+"\n";
			text += "\n\n";

			text += "Project Title: "+collaboration.getTitle()+"\n";
			text += "Project URL: http://proteomicsresource.washington.edu/pr/viewProject.do?ID="+collaboration.getID()+"\n\n";


			// System.out.println(text);
			message.setText(text);

			// send the message
			Transport.send(message);

		} catch (Exception e) { log.error("Error sending email to vsharma" , e); }
	}

	static void emailVsharmaNoReviewersFound(Collaboration collaboration) {

		log.info("Sending email to vsharma. No reviewer pair found for project: "+collaboration.getID());

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

			Researcher vsharma = new Researcher();
			vsharma.load(1811);

			// set the to address
			String emailStr = vsharma.getEmail();
			log.info("Sending email to: "+emailStr);

			Address[] toAddress = InternetAddress.parse(emailStr);
			message.setRecipients(Message.RecipientType.TO, toAddress);

			// set the subject
			message.setSubject("NO REVIEWER PAIR FOUND: UWPR - Collaboration Request #" + collaboration.getID());

			// set the message body
			String text = "";

			text += "Project Title: "+collaboration.getTitle()+"\n";
			text += "Project URL: http://proteomicsresource.washington.edu/pr/viewProject.do?ID="+collaboration.getID()+"\n\n";


			// System.out.println(text);
			message.setText(text);

			// send the message
			Transport.send(message);

		} catch (Exception e) { log.error("Error sending email to vsharma" , e); }
	}
}
