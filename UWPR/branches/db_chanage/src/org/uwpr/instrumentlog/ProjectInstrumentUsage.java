package org.uwpr.instrumentlog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProjectInstrumentUsage extends UsageRange<UsageBlockBase> {

	private int projectID;
	private String projectTitle;
	private String piName;
	private int piID;
	private int instrumentID;
	private String instrumentName;
	
    
    
	private List <UsageBlockBase> usageBlocks;
	
	public ProjectInstrumentUsage(int projectID, String projectName, int piID, String piName,
			int instrumentID, String instrumentName, Timestamp startDate, Timestamp endDate) {
		super(startDate, endDate);
		this.projectID = projectID;
		this.projectTitle = projectName;
		this.piName = piName;
		this.piID = piID;
		this.instrumentID = instrumentID;
		this.instrumentName = instrumentName;
		usageBlocks = new ArrayList<UsageBlockBase>();
	}

	/**
	 * @return the projectID
	 */
	public int getProjectID() {
		return projectID;
	}

	/**
	 * @return the instrumentID
	 */
	public int getInstrumentID() {
		return instrumentID;
	}
	
	public String getInstrumentName() {
	    return instrumentName;
	}

	/**
	 * @return the usageBlocks
	 */
	public List<UsageBlockBase> getAllUsageBlocks() {
		return usageBlocks;
	}
	
	public void addUsageBlock(UsageBlockBase block) {
		if (!usageBlocks.contains(block))
			usageBlocks.add(block);
	}
	
	/**
	 * @return the projectTitle
	 */
	public String getProjectTitle() {
		return projectTitle;
	}

	/**
	 * @param projectTitle the project to set
	 */
	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}

	/**
	 * @return the projectPI
	 */
	public String getProjectPI() {
		return piName;
	}

	/**
	 * @return the ID of the projectPI
	 */
	public int getPIID() {
		return piID;
	}
	
	/**
	 * @param projectPI the projectPI to set
	 */
	public void setProjectPI(String projectPI) {
		this.piName = projectPI;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("ProjectInstrumentUsage\n");
		buf.append("Project ID: "+projectID+"\n");
		buf.append("Project Title: "+projectTitle+"\n");
		buf.append("Project PI: "+piName+"\n");
		buf.append("Instrument ID: "+instrumentID+"\n");
		buf.append("StartDate: "+getStartTime().toString()+"; EndDate: "+getEndTime().toString()+"\n");
		for (UsageBlockBase block: usageBlocks)
			buf.append(block.toString()+"\n");
		return buf.toString();
		
	}
}
