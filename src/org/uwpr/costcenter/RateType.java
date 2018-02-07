/**
 * RateType.java
 * @author Vagisha Sharma
 * Apr 29, 2011
 */
package org.uwpr.costcenter;

import java.math.BigDecimal;

/**
 * Encapsulates a type of rate that will be charged for instrument use. 
 * e.g. Internal, Commercial etc. 
 */
public class RateType {

	private int id;
	private String name;
	private String description;
	private BigDecimal setupFee;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getSetupFee() {
		return setupFee;
	}

	public void setSetupFee(BigDecimal setupFee) {
		this.setupFee = setupFee;
	}
}
