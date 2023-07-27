/**
 * 
 */
package org.yeastrc.project.payment;

import org.apache.commons.lang.StringUtils;
import org.uwpr.www.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * PaymentMethod.java
 * @author Vagisha Sharma
 * May 5, 2011
 * 
 */
public class PaymentMethod {

	private int id;
	private String uwbudgetNumber;
	private Date budgetExpirationDate;
	private String ponumber;
	public String paymentMethodName; // This will also be the worktag description
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
	private int creatorId;
	private Timestamp createDate;
	private Timestamp lastUpdateDate;
	private boolean isCurrent;
	private boolean federalFunding;
    private BigDecimal poAmount;

	private String worktag; // Required. Format: [GR|GF|PG|CC|SAG]######
	private String resourceWorktag; // Required for PG, CC GR, and some SAG worktags. Format: RS######
	private String resourceWorktagDescr;
	private String assigneeWorktag; // Format: AS######
	private String assigneeWorktagDescr;
	private String activityWorktag; // Format: AC######
	private String activityWorktagDescr;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getUwbudgetNumber() {
		return uwbudgetNumber;
	}
	public void setUwbudgetNumber(String uwbudgetNumber) {
		this.uwbudgetNumber = uwbudgetNumber;
	}

	public Date getBudgetExpirationDate()
	{
		return budgetExpirationDate;
	}

	public void setBudgetExpirationDate(Date budgetExpirationDate)
	{
		this.budgetExpirationDate = budgetExpirationDate;
	}

	public String getPonumber() {
		return ponumber;
	}
	public void setPonumber(String ponumber) {
		this.ponumber = ponumber;
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
	public int getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}
	public boolean isCurrent() {
		return isCurrent;
	}
	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}
	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	public Timestamp getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(Timestamp lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	public boolean isFederalFunding() {
		return federalFunding;
	}
	public void setFederalFunding(boolean federalFunding) {
		this.federalFunding = federalFunding;
	}

    public BigDecimal getPoAmount() {
        return poAmount != null ? poAmount : BigDecimal.ZERO;
    }

    public void setPoAmount(BigDecimal poAmount) {
        this.poAmount = poAmount;
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

	public String getResourceWorktagNotNull() {
		return !StringUtils.isBlank(resourceWorktag) ? resourceWorktag : "";
	}

	public void setResourceWorktag(String resourceWorktag) {
		this.resourceWorktag = resourceWorktag;
	}

	public String getResourceWorktagDescr() {
		return resourceWorktagDescr;
	}

	public String getResourceWorktagDescrNotNull() {
		return !StringUtils.isBlank(resourceWorktagDescr) ? resourceWorktagDescr : "";
	}

	public void setResourceWorktagDescr(String resourceWorktagDescr) {
		this.resourceWorktagDescr = resourceWorktagDescr;
	}

	public String getAssigneeWorktag() {
		return assigneeWorktag;
	}

	public String getAssigneeWorktagNotNull() {
		return !StringUtils.isBlank(assigneeWorktag) ? assigneeWorktag : "";
	}

	public void setAssigneeWorktag(String assigneeWorktag) {
		this.assigneeWorktag = assigneeWorktag;
	}

	public String getAssigneeWorktagDescr() {
		return assigneeWorktagDescr;
	}

	public String getAssigneeWorktagDescrNotNull() {
		return !StringUtils.isBlank(assigneeWorktagDescr) ? assigneeWorktagDescr : "";
	}

	public void setAssigneeWorktagDescr(String assigneeWorktagDescr) {
		this.assigneeWorktagDescr = assigneeWorktagDescr;
	}

	public String getActivityWorktag() {
		return activityWorktag;
	}

	public String getActivityWorktagNotNull() {
		return !StringUtils.isBlank(activityWorktag) ? activityWorktag : "";
	}

	public void setActivityWorktag(String activityWorktag) {
		this.activityWorktag = activityWorktag;
	}

	public String getActivityWorktagDescr() {
		return activityWorktagDescr;
	}

	public String getActivityWorktagDescrNotNull() {
		return !StringUtils.isBlank(activityWorktagDescr) ? activityWorktagDescr : "";
	}

	public void setActivityWorktagDescr(String activityWorktagDescr) {
		this.activityWorktagDescr = activityWorktagDescr;
	}

	public String getDisplayString()
    {
		StringBuilder displayString = new StringBuilder();
		displayString.append(getShortDisplayString());

		String name = getName50Chars();
		if(!StringUtils.isBlank(name))
		{
			displayString.append(", ").append(name);
		}

		if(budgetExpirationDate != null)
		{
			displayString.append(", exp: " + TimeUtils.shortDate.format(budgetExpirationDate));
		}
        return displayString.toString();
    }

	public String getShortDisplayString()
	{
		StringBuilder displayString = new StringBuilder();
		if(!StringUtils.isBlank(getUwbudgetNumber()))
		{
			displayString.append("UW: ").append(getUwbudgetNumber());
		}
		else if(!StringUtils.isBlank(getPonumber()))
		{
			displayString.append("PO: ").append(getPonumber());
		}
		else if (!StringUtils.isBlank(getWorktag()))
		{
			displayString.append("Worktag: ").append(getWorktag());
		}
		else
		{
			return "WORKTAG, BUDGET NUMBER OR PO NUMBER NOT FOUND";
		}

		return displayString.toString();
	}

	public String getName50Chars()
	{
		if(!StringUtils.isBlank(paymentMethodName))
		{
			// Truncate if necessary
			return paymentMethodName.length() > 50 ? paymentMethodName.substring(0,46) + "..." : paymentMethodName;
		}
		return "";
	}
}
