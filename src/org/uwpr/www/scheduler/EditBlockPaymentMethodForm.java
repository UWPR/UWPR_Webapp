/**
 * EditProjectInstrumentTimeForm.java
 * @author Vagisha Sharma
 * Jan 6, 2012
 */
package org.uwpr.www.scheduler;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.project.payment.ProjectPaymentMethodDAO;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 */
public class EditBlockPaymentMethodForm extends ActionForm {

    private int projectId;
    private int instrumentId;
    private String instrumentName;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;

    private String usageBlockIdsToEdit;
    private List<PaymentPercent> paymentPercentList;
	
	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();

		if(paymentPercentList == null)
        {
            errors.add("costcenter", new ActionMessage("error.costcenter.invaliddata", "No payment methods found."));
        }

        else
        {
            int total = 0;
            Set<Integer> paymentMethods = new HashSet<Integer>();
            for(PaymentPercent payment: paymentPercentList)
            {
                try
                {
                    int percent = payment.getPaymentPercentInteger();
                    if(percent <= 0)
                    {
                        errors.add("costcenter", new ActionMessage("error.costcenter.invaliddata", "Please enter a percent value greater than 0."));
                    }
                    total += percent;
                    payment.setPaymentPercent(String.valueOf(percent));
                }
                catch(NumberFormatException e)
                {
                    errors.add("costcenter", new ActionMessage("error.costcenter.invaliddata", "Invalid value for payment percent: " + payment.getPaymentPercent()));
                }

                if(payment.getPaymentMethodId() <= 0)
                {
                    errors.add("costcenter", new ActionMessage("error.costcenter.invaliddata", "Please select a payment method."));
                }

                if(paymentMethods.contains(payment.getPaymentMethodId()))
                {
                    errors.add("costcenter", new ActionMessage("error.costcenter.invaliddata", "Selected payment methods should not be the same."));
                }
                paymentMethods.add(payment.getPaymentMethodId());
            }

            if(total != 100)
            {
                errors.add("costcenter", new ActionMessage("error.costcenter.invaliddata", "Total does not add up to 100%."));
            }
        }
        if(!errors.isEmpty())
        {
            // get a list of ALL payment methods for this project
            List<PaymentMethod> paymentMethods = null;
            try {
                paymentMethods = ProjectPaymentMethodDAO.getInstance().getCurrentPaymentMethods(projectId);
            } catch (SQLException e) {
                errors.add("costcenter", new ActionMessage("error.costcenter.load", "Error loading payment methods for project"));
            }
            request.setAttribute("paymentMethods", paymentMethods);
        }
        return errors;
	}

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request)
    {
        super.reset(mapping, request);

        // This has to be reset otherwise, previously selected, and then removed, payment methods
        // are present in the list.
        // To reproduce the error:
        // 1. Click on "Change Payment Methods" link in the scheduler
        // 2. Add a second payment method such that validate will fail (e.g. total percent does not add up to 100%)
        // 3. Remove the second payment method and click "Update"
        // paymentPercentList still contains the second payment method even though it was removed.
        // I don't know the reason for this behaviour.  Resetting to null in this method solves the problem.
        this.paymentPercentList = null;
    }

    public List<PaymentPercent> getPaymentPercentList() {
        return paymentPercentList;
    }

    public void setPaymentPercentList(List<PaymentPercent> paymentPercentList) {
        this.paymentPercentList = paymentPercentList;
    }

    public PaymentPercent getPaymentPercentItem(int index)
    {
        if (this.paymentPercentList == null)
        {
            this.paymentPercentList = new ArrayList<PaymentPercent>();
        }
        while (index >= this.paymentPercentList.size())
        {
            this.paymentPercentList.add( new PaymentPercent() );
        }
        return (PaymentPercent) this.paymentPercentList.get(index);
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(int instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getUsageBlockIdsToEdit() {
        return usageBlockIdsToEdit;
    }

    public void setUsageBlockIdsToEdit(String usageBlockIdsToEdit) {
        this.usageBlockIdsToEdit = usageBlockIdsToEdit;
    }

    public static final class PaymentPercent
    {
        private int paymentMethodId;
        private String label;
        private String paymentPercent;

        public int getPaymentMethodId() {
            return paymentMethodId;
        }

        public void setPaymentMethodId(int paymentMethodId) {
            this.paymentMethodId = paymentMethodId;
        }

        public String getPaymentPercent() {
            return paymentPercent;
        }

        public int getPaymentPercentInteger()
        {
            if(paymentPercent != null)
            {
                return new BigDecimal(getPaymentPercent()).intValue();
            }
            return 0;
        }

        public void setPaymentPercent(String paymentPercent) {
            this.paymentPercent = paymentPercent;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
