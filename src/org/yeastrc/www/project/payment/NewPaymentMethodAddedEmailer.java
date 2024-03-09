/**
 * NewPaymentMethodAddedEmailer.java
 * @author Vagisha Sharma
 * Jun 23, 2011
 */
package org.yeastrc.www.project.payment;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uwpr.AdminUtils;
import org.uwpr.AppProperties;
import org.uwpr.www.EmailUtils;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.Researcher;
import org.yeastrc.project.payment.PaymentMethod;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.sql.SQLException;
import java.util.List;

/**
 * 
 */
public class NewPaymentMethodAddedEmailer {

	private static NewPaymentMethodAddedEmailer instance = new NewPaymentMethodAddedEmailer();
	
	private static final Logger log = LogManager.getLogger(NewPaymentMethodAddedEmailer.class);
	
	private NewPaymentMethodAddedEmailer() {}
	
	public static NewPaymentMethodAddedEmailer getInstance() {
		return instance;
	}
	
	public void sendEmail(PaymentMethod paymentMethod, int projectId) {
		
		log.info("Sending new payment method email to admins");
		List<Researcher> admins = AdminUtils.getNotifyAdmins();

		Researcher creator = new Researcher();
		try {
			creator.load(paymentMethod.getCreatorId());
		} catch (InvalidIDException e) {
			log.error("No researcher found for ID: "+paymentMethod.getCreatorId(), e);
		} catch (SQLException e) {
			log.error("Error loading reseracher for ID: "+paymentMethod.getCreatorId(), e);
		}

		try {
			// set the to address
			String emailStr = "";
			for(Researcher r: admins) {
				emailStr += ","+r.getEmail();
			}
			if(emailStr.length() > 0) emailStr = emailStr.substring(1); // remove first comma

			Address[] toAddress = InternetAddress.parse(emailStr);

			// set the message body
			StringBuilder text = new StringBuilder();
			text.append("A new payment method has been added to project ID: "+projectId+"\n");
			text.append("Project URL: " + AppProperties.getHost() + "/pr/viewProject.do?ID="+projectId+"\n");
			text.append("\n");
			
			
			text.append("Payment method ID: "+paymentMethod.getId()+"\n");
			text.append("URL: " + AppProperties.getHost() + "/pr/viewPaymentMethod.do?projectId="+projectId+
					"&paymentMethodId="+paymentMethod.getId()+"\n");
			text.append("Created by: "+creator.getFirstName()+" "+creator.getLastName()+"\n");
			if(!StringUtils.isBlank(paymentMethod.getUwbudgetNumber())) {
				text.append("UW Budget number: "+paymentMethod.getUwbudgetNumber()+"\n");
			}
			if(!StringUtils.isBlank(paymentMethod.getPonumber())) {
				text.append("PO number: "+paymentMethod.getPonumber()+"\n");
			}
			if(!StringUtils.isBlank(paymentMethod.getWorktag())) {
				text.append("Worktag: "+paymentMethod.getWorktag()+"\n");
			}
			text.append("Contact information: \n");
			text.append("Name: "+paymentMethod.getContactFirstName()+" ");
			text.append(paymentMethod.getContactLastName()+"\n");
			text.append("Email: "+paymentMethod.getContactEmail()+"\n");
			text.append("Phone: "+paymentMethod.getContactPhone()+"\n");
			
			
			text.append("\n\nThank you,\nThe UW Proteomics Resource\n");

			System.out.println(text);

			// send the message
			EmailUtils.sendMail("UWPR - A new payment method has been added to project ID " + projectId, text.toString(), toAddress);

		} catch (Exception e) { log.error("Error sending email about a new payment method added" , e); }
	}
}
