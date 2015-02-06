package org.uwpr.instrumentlog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MsInstrumentUsage extends UsageRange<UsageBlockBase> {

	private int instrumentID;
	private String instrumentName;
	private boolean active;
	private List <ProjectInstrumentUsage> projectUsageList;
	
	private static final int topCount = 3; 
	
	public MsInstrumentUsage(int instrumentID, String instrumentName, boolean active,
			                 Timestamp startDate, Timestamp endDate) {
		super(startDate, endDate);
		this.instrumentID = instrumentID;
		this.instrumentName = instrumentName;
		this.active = active;
		this.projectUsageList = new ArrayList<ProjectInstrumentUsage>();
	}

	/**
	 * @return the instrumentID
	 */
	public int getInstrumentID() {
		return instrumentID;
	}

	/**
	 * @return the instrumentName
	 */
	public String getInstrumentName() {
		return instrumentName;
	}
	
	public boolean isActive() {
		return active;
	}
	
	/**
	 * @return the topProjectUsage
	 */
	public List<ProjectInstrumentUsage> getProjectUsageList() {
		return projectUsageList;
	}
	
	public List <ProjectInstrumentUsage> getTopProjects() {
		return projectUsageList.subList(0, Math.min(topCount, projectUsageList.size()));
	}
	
	public void setProjectUsageList(List <ProjectInstrumentUsage> usageList) {
		if (usageList == null)	return;
		this.projectUsageList = usageList;
		
		// sort the list in descending order of usage
		Collections.sort(projectUsageList, new Comparator<ProjectInstrumentUsage>(){
			public int compare(ProjectInstrumentUsage o1, ProjectInstrumentUsage o2) {
				return Float.valueOf(o2.getNumHoursUsed()).compareTo(o1.getNumHoursUsed()); // sort descending
			}});
	}
	
	protected List <UsageBlockBase> getAllUsageBlocks() {
		if (projectUsageList.size() == 0)
			new ArrayList<UsageBlock>(0);
		
		// make a list of all usage blocks for all projects.
		List <UsageBlockBase> allBlks = new ArrayList<UsageBlockBase>();
		for (ProjectInstrumentUsage usage: projectUsageList)
			allBlks.addAll(usage.getAllUsageBlocks());
		
		return allBlks;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("MsInstrumentUsageSummary\n");
		buf.append("Instrument ID: "+instrumentID+"; Name: "+instrumentName);
		buf.append("\n\n");
		buf.append("StartDate: "+getStartTime().toString()+"; EndDate: "+getEndTime().toString());
		buf.append("\n");
		for (ProjectInstrumentUsage projUsage: projectUsageList)
			buf.append(projUsage.toString()+"\n");
		return buf.toString();
	}
	
	public void sortByProjectID() {
		Collections.sort(projectUsageList, new ProjectIDComparator());
	}
	
	public void sortByProjectTitle() {
		Collections.sort(projectUsageList, new ProjectTitleComparator());
	}
	
	public void sortByPI() {
		Collections.sort(projectUsageList, new ProjectPIComparator());
	}
	
	public void sortByUsage() {
		Collections.sort(projectUsageList, new ProjectUsageComparator());
	}
	
	
	private static class ProjectIDComparator implements Comparator<ProjectInstrumentUsage> {
		public int compare(ProjectInstrumentUsage p1, ProjectInstrumentUsage p2) {
			int p1ID = p1.getProjectID();
			int p2ID = p2.getProjectID();
			return p1ID < p2ID ? -1 : (p1ID == p2ID ? 0 : 1);
		}
	}
	
	private static class ProjectTitleComparator implements Comparator<ProjectInstrumentUsage> {
		public int compare(ProjectInstrumentUsage p1, ProjectInstrumentUsage p2) {
			return p1.getProjectTitle().compareTo(p2.getProjectTitle());
		}
	}
	
	private static class ProjectPIComparator implements Comparator<ProjectInstrumentUsage> {
		public int compare(ProjectInstrumentUsage p1, ProjectInstrumentUsage p2) {
			return p1.getProjectPI().compareTo(p2.getProjectPI());
		}
	}
	
	private static class ProjectUsageComparator implements Comparator<ProjectInstrumentUsage> {
		public int compare(ProjectInstrumentUsage p1, ProjectInstrumentUsage p2) {
			float p1Usage = p1.getNumHoursUsed();
			float p2Usage = p2.getNumHoursUsed();
			return (p1Usage < p2Usage ? -1 : (p1Usage == p2Usage ? 0 : 1));
		}
	}
}
