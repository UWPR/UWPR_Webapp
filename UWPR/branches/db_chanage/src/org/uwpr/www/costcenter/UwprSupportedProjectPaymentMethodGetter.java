/**
 * 
 */
package org.uwpr.www.costcenter;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.project.payment.PaymentMethodDAO;

/**
 * UwprSupportedProjectPaymentMethodGetter.java
 * @author Vagisha Sharma
 * Sep 27, 2011
 * 
 */
public class UwprSupportedProjectPaymentMethodGetter {

	private UwprSupportedProjectPaymentMethodGetter() {}
	
	private static final int MAINTENANCE_PROJECT = 42;
	
	private static final Logger log = Logger.getLogger(UwprSupportedProjectPaymentMethodGetter.class);
	
	public static PaymentMethod get(int projectId) {
		
		PaymentMethodDAO dao = PaymentMethodDAO.getInstance();
		
		List<PaymentMethod> paymentMethods = null;
		
		try {
			paymentMethods = dao.getPaymentMethods("UWPR", "UWPR");
		} catch (SQLException e) {
			
			log.error("There was en error getting payment method for UWPR supported projects", e);
			return null;
		}
		
		if(paymentMethods == null || paymentMethods.size() == 0) {
			log.error("No payment method found for UWPR supported projects");
			return null;
		}
		
		if(projectId == MAINTENANCE_PROJECT) {
			for(PaymentMethod method: paymentMethods) {
				if(method.getUwbudgetNumber().equals("14-5220")) {
					return method;
				}
			}
		}
		else {
			for(PaymentMethod method: paymentMethods) {
				if(method.getUwbudgetNumber().equals("07-5229")) {
					return method;
				}
			}
		}
		log.error("No payment method found for UWPR supported project "+projectId);
		return null;
		
	}
}
