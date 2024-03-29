/*
 * RegisterForm.java
 *
 * Created on October 17, 2003
 *
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project.payment;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.www.costcenter.PaymentMethodChecker;
import org.uwpr.www.util.TimeUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

public class PaymentMethodForm extends ActionForm {

	private int id;
	private int projectId;
	private int paymentMethodId; // only available when we are editing an existing payment method
	private String uwBudgetNumber;
	private Date budgetExpirationDate;
	private String budgetExpirationDateStr;

	// Worktags
	private String worktag; // Required. Format: [GR|GF|PG|CC|SAG]######
	private String resourceWorktag; // Required for PG, CC GR, and some SAG worktags. Format: RS######
	private String resourceWorktagDescr;
	private String assigneeWorktag; // Format: AS######
	private String assigneeWorktagDescr;
	private String activityWorktag; // Format: AC######
	private String activityWorktagDescr;

	private String poNumber;
	private String paymentMethodName; // Also used for worktag description
	private String contactFirstName;
	private String contactLastName;
	private String contactEmail;
	private String contactPhone;
	private String organization;
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String state;
	private String zip;
	private String country;
	private boolean isCurrent;
	
	private boolean isEditable; // If false only the status field is editable
	private boolean federalFunding = false;

    private String poAmount;
	
	private boolean ponumberAllowed; // Only Non-UW affiliated projects are allowed a PO number.
	private boolean uwbudgetAllowed; // Only UW affiliated projects are allowed a UW Budget number.
	private boolean worktagAllowed; // Worktags are allowed only for UW affiliated projects.

	@Override
	public void	reset(ActionMapping mapping, HttpServletRequest request)  {
		
		this.federalFunding = false; // this is a checkbox; set it to false
	}
	
	/**
	 * Validate the properties that have been sent from the HTTP request,
	 * and return an ActionErrors object that encapsulates any
	 * validation errors that have been found.  If no errors are found, return
	 * an empty ActionErrors object.
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();

		// We need atleast a Worktag or UW budget number OR a PO number
		if (isWorktagAllowed() && StringUtils.isBlank(worktag))
		{
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Worktag is required"));
		}
		if (isUwbudgetAllowed() && StringUtils.isBlank(uwBudgetNumber))
		{
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "UW budget number is required"));
		}
		if (isPonumberAllowed() && StringUtils.isBlank(poNumber))
		{
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "PO number is required"));
		}

		PaymentMethodChecker checker = PaymentMethodChecker.getInstance();
		if(!StringUtils.isBlank(uwBudgetNumber) && !checker.checkUwbudgetNumber(uwBudgetNumber))
		{
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Invalid UW budget number."));
		}

		else if (!StringUtils.isBlank(worktag) && !checker.isValidWorktag(worktag))
		{
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Invalid worktag. " + PaymentMethodChecker.getWorktagFormatMessage()));
		}

		if (StringUtils.isBlank(resourceWorktag) && !StringUtils.isBlank(worktag) && checker.requiresResourceTag(worktag))
		{
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Resource worktag is required for CC worktags."));
		}
		if (!(StringUtils.isBlank(resourceWorktag) || checker.isValidResourceWorktag(resourceWorktag)))
		{
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Invalid Resource worktag. " + PaymentMethodChecker.getResourceWorktagFormatMessage()));
		}

		if (!(StringUtils.isBlank(assigneeWorktag) || checker.isValidAssigneeWorktag(assigneeWorktag)))
		{
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Invalid Assignee worktag. " + PaymentMethodChecker.getAssigneeWorktagFormatMessage()));
		}

		if (!(StringUtils.isBlank(activityWorktag) || checker.isValidActivityWorktag(activityWorktag)))
		{
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Invalid Activity worktag. " + PaymentMethodChecker.getActivityWorktagFormatMessage()));
		}

		if(isUwbudgetAllowed() || isWorktagAllowed()) {
			if(StringUtils.isBlank(budgetExpirationDateStr))
			{
				errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Expiration date is required."));
			}
			else
			{
				try
				{
					budgetExpirationDate = TimeUtils.shortDate.parse(budgetExpirationDateStr);
				} catch (ParseException e)
				{
					errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Invalid expiration date. Accepted date format is MM/dd/yyyy"));
				}
			}
		}

		if(!StringUtils.isBlank(poNumber)) {
			// remove any spaces
			poNumber = poNumber.trim().replaceAll("\\s", "");
			if(!PaymentMethodChecker.getInstance().checkPonumber(poNumber)) {
				errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Invalid PO number."));
			}

            // PO numbers require a dollar amount
            if(StringUtils.isBlank(poAmount))
            {
                errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Enter an amount for the PO."));
            }
            else
            {
                try
                {
                    BigDecimal amount = new BigDecimal(poAmount);
                    if(amount.doubleValue() <= 0)
                    {
                        errors.add("payment", new ActionMessage("error.payment.infoincomplete", "PO amount has to be greater than 0."));
                    }
                }
                catch(NumberFormatException e)
                {
                    errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Invalid PO amount."));
                }
            }
		}
		
		if(StringUtils.isBlank(contactFirstName)) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "First name"));
		}
		
		if(StringUtils.isBlank(contactLastName)) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Last name"));
		}
		
		// email
		if(StringUtils.isBlank(contactEmail)) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "E-mail address"));
		}
		else if(!contactEmail.contains("@") || contactEmail.length() < 3) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "E-mail address appears invalid"));
		}
		
		// phone number
		if(StringUtils.isBlank(contactPhone)) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Phone number"));
		}
		else {
			// need at least 10 digits in the phone number
			String myStr = contactPhone.replaceAll( "[^\\d]", "" );
			if(myStr.length() < 10) {
				errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Phone number appears invalid"));
			}
		}
		
		
		if(StringUtils.isBlank(organization)) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Organization"));
		}
		
		if(StringUtils.isBlank(addressLine1)) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Address"));
		}
		
		if(StringUtils.isBlank(city)) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "City"));
		}
		
		if(StringUtils.isBlank(state)) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "State"));
		}
		
		if(StringUtils.isBlank(zip)) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Zip"));
		}
		else if(zip.length() < 5) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Zip code appears invalid"));
		}
		
		if(StringUtils.isBlank(country)) {
			errors.add("payment", new ActionMessage("error.payment.infoincomplete", "Country"));
		}

		return errors;
	}


	public String getUwBudgetNumber() {
		return uwBudgetNumber;
	}

	public void setUwBudgetNumber(String uwBudgetNumber) {
		this.uwBudgetNumber = uwBudgetNumber;
	}

	public String getBudgetExpirationDateStr()
	{
		return budgetExpirationDateStr;
	}

	public void setBudgetExpirationDateStr(String budgetExpirationDateStr)
	{
		this.budgetExpirationDateStr = budgetExpirationDateStr;
	}

	public Date getBudgetExpirationDate()
	{
		return budgetExpirationDate;
	}

	public void setBudgetExpirationDate(Date budgetExpirationDate)
	{
		this.budgetExpirationDate = budgetExpirationDate;
		budgetExpirationDateStr = budgetExpirationDate == null ? "" : TimeUtils.shortDate.format(budgetExpirationDate);
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

	public String getPaymentMethodName()
	{
		return paymentMethodName;
	}

	public void setPaymentMethodName(String paymentMethodName)
	{
		this.paymentMethodName = paymentMethodName;
	}

	public String getContactFirstName() {
		return contactFirstName;
	}

	public void setContactFirstName(String contactFirstName) {
		this.contactFirstName = contactFirstName;
	}

	public String getContactLastName() {
		return contactLastName;
	}

	public void setContactLastName(String contactLastName) {
		this.contactLastName = contactLastName;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getOrganization() {
		return organization;
	}
	
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public int getPaymentMethodId() {
		return paymentMethodId;
	}

	public void setPaymentMethodId(int paymentMethodId) {
		this.paymentMethodId = paymentMethodId;
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public boolean isFederalFunding() {
		return federalFunding;
	}

	public void setFederalFunding(boolean federalFunding) {
		this.federalFunding = federalFunding;
	}
	
	public boolean isPonumberAllowed() {
		return ponumberAllowed;
	}

	public void setPonumberAllowed(boolean ponumberAllowed) {
		this.ponumberAllowed = ponumberAllowed;
	}

	public boolean isUwbudgetAllowed() {
		return uwbudgetAllowed;
	}

	public void setUwbudgetAllowed(boolean uwbudgetAllowed) {
		this.uwbudgetAllowed = uwbudgetAllowed;
	}

	public boolean isWorktagAllowed() {
		return worktagAllowed;
	}

	public void setWorktagAllowed(boolean worktagAllowed) {
		this.worktagAllowed = worktagAllowed;
	}

	public String getPoAmount() {
        return poAmount;
    }

    public void setPoAmount(String poAmount) {
        this.poAmount = poAmount;
    }

    public BigDecimal getPoBigDecimalValue()
    {
        if(!StringUtils.isBlank(poAmount))
        {
            return new BigDecimal(poAmount);
        }
        return null;
    }

    public void setPoBigDecimalValue(BigDecimal value)
    {
        if(value != null)
        {
            poAmount = value.toString();
        }
    }

	public String getWorktag() {
		return worktag;
	}

	public void setWorktag(String worktag) {
		this.worktag = worktag;
	}

	public String getResourceWorktag() {
		return resourceWorktag;
	}

	public void setResourceWorktag(String resourceWorktag) {
		this.resourceWorktag = resourceWorktag;
	}

	public String getResourceWorktagDescr() {
		return resourceWorktagDescr;
	}

	public void setResourceWorktagDescr(String resourceWorktagDescr) {
		this.resourceWorktagDescr = resourceWorktagDescr;
	}

	public String getAssigneeWorktag() {
		return assigneeWorktag;
	}

	public void setAssigneeWorktag(String assigneeWorktag) {
		this.assigneeWorktag = assigneeWorktag;
	}

	public String getAssigneeWorktagDescr() {
		return assigneeWorktagDescr;
	}

	public void setAssigneeWorktagDescr(String assigneeWorktagDescr) {
		this.assigneeWorktagDescr = assigneeWorktagDescr;
	}

	public String getActivityWorktag() {
		return activityWorktag;
	}

	public void setActivityWorktag(String activityWorktag) {
		this.activityWorktag = activityWorktag;
	}

	public String getActivityWorktagDescr() {
		return activityWorktagDescr;
	}

	public void setActivityWorktagDescr(String activityWorktagDescr) {
		this.activityWorktagDescr = activityWorktagDescr;
	}

	public void clearAllFields()
	{
		clearAllWorktags();
		clearUwBudgetNumber();
		clearPoNumber();
	}

	public void clearPoNumber()
	{
		this.paymentMethodName = null;
		this.poNumber = null;
		this.poAmount = null;
	}

	public void clearUwBudgetNumber()
	{
		this.uwBudgetNumber = null;
		this.budgetExpirationDate = null;
		this.budgetExpirationDateStr = null;
		this.paymentMethodName = null;
	}

	public void clearWorktag()
	{
		this.budgetExpirationDate = null;
		this.budgetExpirationDateStr = null;
		this.paymentMethodName = null;
		this.worktag = null;
	}

	public void clearAllWorktags()
	{
		this.clearWorktag();
		this.resourceWorktag = null;
		this.resourceWorktagDescr = null;
		this.assigneeWorktag = null;
		this.assigneeWorktagDescr = null;
		this.activityWorktag = null;
		this.activityWorktagDescr = null;
	}
}