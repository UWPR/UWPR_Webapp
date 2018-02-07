/**
 * 
 */
package org.yeastrc.project.payment;

import org.apache.commons.lang.StringUtils;
import org.uwpr.costcenter.Cost;
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
	public String paymentMethodName;
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

    private Cost invoicedCost;
	private Cost cost;

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

    public BigDecimal getTotalCost() {
		return cost == null ? BigDecimal.ZERO : cost.getTotal();
    }

	public BigDecimal getInstrumentCost()
	{
		return cost == null ? BigDecimal.ZERO : cost.instrumentCost;
	}

	public BigDecimal getSignupCost()
	{
		return cost == null ? BigDecimal.ZERO : cost.signupCost;
	}

	public BigDecimal getSetupCost()
	{
		return cost == null ? BigDecimal.ZERO : cost.setupCost;
	}

    public void setCost(Cost cost) {
        this.cost = cost;
    }

    public BigDecimal getInvoicedCost() {
        return invoicedCost != null ? BigDecimal.ZERO : invoicedCost.getTotal();
    }

    public void setInvoicedCost(Cost invoicedCost) {
        this.invoicedCost = invoicedCost;
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
		else
		{
			return "BUDGET NUMBER OR PO NUMBER NOT FOUND";
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
