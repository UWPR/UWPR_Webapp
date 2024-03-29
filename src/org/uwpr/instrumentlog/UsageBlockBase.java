package org.uwpr.instrumentlog;

import org.uwpr.www.util.TimeUtils;

import java.util.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class UsageBlockBase implements Block
{

	private int id;
	private int instrumentID;
	private int projectID;
	private int researcherID; // researcher who created this usage block
	private int updaterResearcherID; // researcher who updated this usage block
	private int instrumentOperatorId; // researcher who will operate the instrument
	private int instrumentRateID; 
	private Date startDate;
	private Date endDate;
	private Date dateCreated;
	private Date dateChanged;
	private String notes;
	private boolean deleted = false;
	private boolean setupBlock = false;
	
	private static DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	public UsageBlockBase(int id, int instrumentID, int projectID) {
	    super();
        this.id = id;
        this.instrumentID = instrumentID;
        this.projectID = projectID;
    }
	
	public UsageBlockBase() {
	    this.notes = "";
	}

	/**
     * Returns the ID of this UsageBlock
     * @return
     */
	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getInstrumentID() {
		return instrumentID;
	}

	public void setInstrumentID(int instrumentID) {
		this.instrumentID = instrumentID;
	}

	public int getInstrumentRateID() {
		return instrumentRateID;
	}

	public void setInstrumentRateID(int instrumentRateID) {
		this.instrumentRateID = instrumentRateID;
	}

	public int getProjectID() {
		return projectID;
	}

	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}

	public int getResearcherID() {
		return researcherID;
	}

	public void setResearcherID(int researcherID) {
		this.researcherID = researcherID;
	}
	
	public int getUpdaterResearcherID() {
		return updaterResearcherID;
	}

	public void setUpdaterResearcherID(int updaterResearchID) {
		this.updaterResearcherID = updaterResearchID;
	}

	public int getInstrumentOperatorId()
	{
		return instrumentOperatorId;
	}

	public void setInstrumentOperatorId(int instrumentOperatorId)
	{
		this.instrumentOperatorId = instrumentOperatorId;
	}

	/**
     * @return the startDate
     */
	public Date getStartDate() {
		return startDate;
	}

	public String getStartDateFormated() {
        return df.format(getStartDate());
    }
	
	/**
     * @param startDate the startDate to set
     */
	public void setStartDate(Date startDate)
	{
		this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
	}

	/**
     * @return the endDate
     */
	public Date getEndDate()
	{
		return endDate;
	}
	
	public String getEndDateFormated() {
        return df.format(getEndDate());
    }

	/**
     * @param endDate the endDate to set
     */
	public void setEndDate(Date endDate)
	{
		this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated != null ? new Date(dateCreated.getTime()) : null;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged)
	{
		this.dateChanged = dateChanged != null ? new Date(dateChanged.getTime()) : null;
	}

	/**
     * Returns the notes entered for this UsageBlock;
     * @return
     */
	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isSetupBlock() {
		return setupBlock;
	}

	public void setSetupBlock(boolean setupBlock) {
		this.setupBlock = setupBlock;
	}

	public int getHours()
	{
		return TimeUtils.getHours(getStartDate(), getEndDate());
	}

	public static int getHours(UsageBlockBase block, List<Integer> accessoryInstrumentIds)
	{
		return accessoryInstrumentIds.contains(block.getInstrumentID()) ? 0 : block.getHours();
	}

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Block ");
		buf.append(TimeUtils.format(getStartDate()) + " - " + TimeUtils.format(getEndDate()));
        buf.append("; projectId: "+getProjectID());
        buf.append("; instrumentId: "+getInstrumentID());

        return buf.toString();
    }
    
    public <T extends UsageBlockBase> void copyTo(T blk) {
        
        blk.setID(id);
        blk.setInstrumentID(instrumentID);
		blk.setInstrumentOperatorId(instrumentOperatorId);
        blk.setInstrumentRateID(instrumentRateID);
        blk.setProjectID(projectID);
        blk.setResearcherID(researcherID);
        blk.setStartDate(startDate);
        blk.setEndDate(endDate);
        blk.setDateCreated(dateCreated);
        blk.setDateChanged(dateChanged);
        blk.setNotes(notes);
		blk.setDeleted(deleted);
		blk.setSetupBlock(setupBlock);
    }
    
    public UsageBlockBase newBlock() {
        return new UsageBlockBase();
    }
	
    public static <T extends UsageBlockBase> List<T> mergeUsageBlocks(List<T> allBlks) {
        // sort the list by startTime
        Collections.sort(allBlks, new Comparator<T> (){
            public int compare(T o1, T o2) {
                return o1.getStartDate().compareTo(o2.getStartDate());
            }});
        List <T> mergedBlks = new ArrayList<T>();
        T currBlk = null;
        for (T blk: allBlks) {
                
            if (overlap(currBlk, blk)) {
                currBlk.setEndDate(DateUtils.maxTimestamp(currBlk.getEndDate(), blk.getEndDate()));
            }
            else {
                currBlk = (T) blk.newBlock();
                currBlk.setStartDate(blk.getStartDate());
                currBlk.setEndDate(blk.getEndDate());
                mergedBlks.add(currBlk);
            }
        }
        return mergedBlks;
    }
    
    // blocks are sorted startDate of blk1 < startDate of blk2
    private static boolean overlap(UsageBlockBase blk1, UsageBlockBase blk2) {
        if (blk1 == null || blk2 == null)   return false;
        // startDate of blk1 should be <= startDate of blk2
        return blk2.getStartDate().getTime() <= blk1.getEndDate().getTime();
    }
}
