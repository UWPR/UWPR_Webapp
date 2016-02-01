/**
 * 
 */
package org.uwpr.instrumentlog;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ProjectInstrumentUsageDAO.java
 * @author Vagisha Sharma
 * May 31, 2011
 * 
 */
public class ProjectInstrumentUsageDAO {

	private static final ProjectInstrumentUsageDAO instance = new ProjectInstrumentUsageDAO();
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	
	private ProjectInstrumentUsageDAO () {}
	
	public static ProjectInstrumentUsageDAO getInstance() {
		return instance;
	}

    //--------------------------------------------------------------------------------------------
    // Project instrument usage
    //--------------------------------------------------------------------------------------------
    public List <ProjectInstrumentUsage> getAllProjectInstrumentUsage(java.util.Date startDate, java.util.Date endDate) throws SQLException {

        UsageBlockFilter filter = new UsageBlockFilter();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setTrimToFit(true);
        List<UsageBlock> usageBlocks = UsageBlockDAO.getUsageBlocks(filter);

        // sort by projectID and then instrumentID
        Collections.sort(usageBlocks, new Comparator<UsageBlock>()
        {
            public int compare(UsageBlock o1, UsageBlock o2)
            {
                if (o1.getProjectID() < o2.getProjectID()) return -1;
                if (o1.getProjectID() > o2.getProjectID()) return 1;
                if (o1.getInstrumentID() < o2.getInstrumentID()) return -1;
                if (o1.getInstrumentID() > o2.getInstrumentID()) return 1;
                return 0;
            }
        });

        List <ProjectInstrumentUsage> projUsageList = new ArrayList<ProjectInstrumentUsage>();

        Timestamp startTs = startDate == null ? null : new Timestamp(startDate.getTime());
        Timestamp endTs = endDate == null ? null : new Timestamp(endDate.getTime());


        ProjectInstrumentUsage projUsage = null;
        for(UsageBlock usageBlock: usageBlocks) {


            if (projUsage == null ||
                    (projUsage.getProjectID() != usageBlock.getProjectID() ||
                            projUsage.getInstrumentID() != usageBlock.getInstrumentID())) {
                projUsage = new ProjectInstrumentUsage(usageBlock.getProjectID(),
                        usageBlock.getProjectTitle(),
                        usageBlock.getPIID(),
                        usageBlock.getProjectPI(),
                        usageBlock.getInstrumentID(),
                        usageBlock.getInstrumentName(),
                        startTs,
                        endTs);
                projUsageList.add(projUsage);
            }

            projUsage.addUsageBlock(usageBlock);
        }
        return projUsageList;
    }
}
