/**
 * 
 */
package org.uwpr.costcenter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.uwpr.instrumentlog.InstrumentUsagePayment;
import org.uwpr.instrumentlog.InstrumentUsagePaymentDAO;
import org.uwpr.instrumentlog.UsageBlockBase;
import org.uwpr.www.costcenter.UwprSupportedProjectPaymentMethodGetter;
import org.yeastrc.project.Collaboration;
import org.yeastrc.project.Project;
import org.yeastrc.project.payment.PaymentMethod;

/**
 * InstrumentUsagePaymentGetter.java
 * @author Vagisha Sharma
 * Sep 3, 2011
 * 
 */
public class InstrumentUsagePaymentGetter {

	private InstrumentUsagePaymentGetter() {}
	
	public static List<InstrumentUsagePayment> get(Project project, UsageBlockBase block) throws SQLException {
		
		List<InstrumentUsagePayment> usagePayments = null; 
		
		// if(project instanceof BilledProject)
		usagePayments = InstrumentUsagePaymentDAO.getInstance().getPaymentsForUsage(block.getID());
		
		// TODO This should be removed.  
		// All scheduled blocks should be associated with a payment method, even the ones for 
		// UWPR supported projects.
		// This is potentially for older blocks, pre cost-center code??
		if(usagePayments == null || usagePayments.size() == 0) {
			
			if (project instanceof Collaboration) {
			
				PaymentMethod paymentMethod = UwprSupportedProjectPaymentMethodGetter.get(project.getID());
				// TODO is this required?
				paymentMethod.setContactEmail("");
				paymentMethod.setContactFirstName("");
				paymentMethod.setContactLastName("");
				paymentMethod.setContactPhone("");
				

				InstrumentUsagePayment iup = new InstrumentUsagePayment();
				iup.setInstrumentUsageId(-1); // dummy ID
				iup.setPaymentMethod(paymentMethod);
				iup.setPercent(new BigDecimal("100.0"));

				usagePayments = new ArrayList<InstrumentUsagePayment>(1);
				usagePayments.add(iup);
			}
		}
		
		return usagePayments;
		
	}
}
