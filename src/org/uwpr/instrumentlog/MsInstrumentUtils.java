package org.uwpr.instrumentlog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.uwpr.costcenter.InstrumentRate;
import org.uwpr.costcenter.InstrumentRateDAO;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.InvalidProjectTypeException;

public class MsInstrumentUtils {

	
	private static MsInstrumentUtils instance = new MsInstrumentUtils();
	
	private MsInstrumentUtils() {}
	
	public static MsInstrumentUtils instance() {
		return instance;
	}
	
	//--------------------------------------------------------------------------------------------
	// MS instrument
	//--------------------------------------------------------------------------------------------
	/**
	 * Returns a list of all instruments (active and retired).
	 */
	public List <MsInstrument> getMsInstruments() {
		return getMsInstruments(false); 
	}
	
	public List <MsInstrument> getMsInstruments(boolean activeOnly) {
		
		// Get our connection to the database.
		Connection conn = null;
		
		try {
			conn = DBConnectionManager.getConnection("pr");
			return getMsInstruments(conn, activeOnly);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		finally {
			// Make sure the connection is returned to the pool
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
		return null;
	}
	
	public List <MsInstrument> getMsInstruments(Connection conn, boolean activeOnly) {
		
		ArrayList<MsInstrument> list = new ArrayList<MsInstrument>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "select * from instruments";
			if(activeOnly) {
				sql += " WHERE active=1";
			}
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				String desc = rs.getString("description");
				boolean active = rs.getBoolean("active");
				list.add(new MsInstrument(id, name, desc, active));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (rs != null) {
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
		}
		
		return list;
	}
	
	public MsInstrument getMsInstrument(int instrumentID) {
		
		// Get our connection to the database.
		Connection conn = null;
		
		try {
			conn = DBConnectionManager.getConnection("pr");
			return getMsInstrument(instrumentID, conn);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		finally {
			// Make sure the connection is returned to the pool
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
		return null;
	}
	
	MsInstrument getMsInstrument(int instrumentID, Connection conn) {
		
		if (conn == null)
			return null;
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "select * from instruments where id="+instrumentID;
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				String desc = rs.getString("description");
				boolean active = rs.getBoolean("active");
				return new MsInstrument(id, name, desc, active);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		finally {

			// Always make sure result sets and statements are closed,
			if (rs != null) {
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------------------------
    // UsageBlockBase
    //--------------------------------------------------------------------------------------------
    public UsageBlockBase getUsageBlockBase(int usageID) {
        // Get our connection to the database.
        Connection conn = null;
        try {
            conn = DBConnectionManager.getConnection("pr");
            return getUsageBlockBase(usageID, conn);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            // Make sure the connection is returned to the pool
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { ; }
                conn = null;
            }
        }
        return null;
    }
    
    UsageBlockBase getUsageBlockBase(int usageID, Connection conn) {
        
        if (conn == null)
            return null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT * from instrumentUsage WHERE id="+usageID;
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                UsageBlockBase blk = makeUsageBlockBase(rs);
                return blk;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        finally {

            // Always make sure result sets and statements are closed,
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { ; }
                rs = null;
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { ; }
                stmt = null;
            }
        }
        
        return null;
    }
    
    private void truncateBlock(UsageBlockBase newBlk, java.util.Date startDate, java.util.Date endDate) {
        if(startDate != null) {
            Date start = newBlk.getStartDate();
            start = start.after(startDate) ? start : startDate; // DateUtils.defaultStartTime(startDate);
            newBlk.setStartDate(start);
        }
        if(endDate != null) {
            Date end = newBlk.getEndDate();
            end = end.before(endDate) ? end : endDate; // DateUtils.defaultEndTime(endDate);
            newBlk.setEndDate(end);
        }
        
    }

    private UsageBlockBase makeUsageBlockBase(ResultSet rs) throws SQLException {
        UsageBlockBase blk = new UsageBlockBase();
        blk.setID(rs.getInt("id"));
        blk.setResearcherID(rs.getInt("enteredBy"));
        blk.setInstrumentID(rs.getInt("instrumentID"));
        blk.setProjectID(rs.getInt("projectID"));
        blk.setStartDate(rs.getTimestamp("startDate"));
        blk.setEndDate(rs.getTimestamp("endDate"));
        blk.setDateCreated(rs.getTimestamp("dateEntered"));
        Integer updaterResearcherId = rs.getInt("updatedBy");
        if(updaterResearcherId != null) {
        	blk.setUpdaterResearcherID(updaterResearcherId);
        }
        blk.setDateChanged(rs.getTimestamp("lastChanged"));
        blk.setNotes(rs.getString("notes"));
        return blk;
    }
    
	//--------------------------------------------------------------------------------------------
	// UsageBlock
	//--------------------------------------------------------------------------------------------
	public UsageBlock getUsageBlock(int usageID) {
		// Get our connection to the database.
		Connection conn = null;
		try {
			conn = DBConnectionManager.getConnection("pr");
			return getUsageBlock(usageID, conn);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			// Make sure the connection is returned to the pool
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
		return null;
	}
	
	UsageBlock getUsageBlock(int usageID, Connection conn) {
		
		if (conn == null)
			return null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = makeUsageSql(usageID);
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();

			if (rs.next()) {
				return makeUsageBlock(rs);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		finally {

			// Always make sure result sets and statements are closed,
			if (rs != null) {
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
		}
		
		return null;
	}
	
	public List<UsageBlock> getAllUsageBlocks(java.util.Date startDate, java.util.Date endDate) throws SQLException {
	    return getAllUsageBlocks(startDate, endDate, true, null); // truncate blocks to fit the start and end dates
	}
	
	public List<UsageBlock> getAllUsageBlocks(java.util.Date startDate, java.util.Date endDate, boolean truncate) throws SQLException {
	    return getAllUsageBlocks(startDate, endDate, truncate, null);
	}
	
	List<UsageBlock> getAllUsageBlocks(java.util.Date startDate, java.util.Date endDate, boolean truncate, String sortBy) throws SQLException {
        return getUsageBlocksForInstrument(-1, startDate, endDate, truncate, sortBy);
    }

	public List<UsageBlock> getUsageBlocksForInstrument(int instrumentID, java.util.Date startDate, java.util.Date endDate) throws SQLException {
	    return getUsageBlocksForInstrument(instrumentID, startDate, endDate, true, null); // truncate blocks to fit the start and end dates
	}
	
	public List<UsageBlock> getUsageBlocksForInstrument(int instrumentID, java.util.Date startDate, java.util.Date endDate,
			                                            boolean truncate) throws SQLException {
	    return getUsageBlocksForInstrument(instrumentID, startDate, endDate, truncate, null);
	}
	
	List<UsageBlock> getUsageBlocksForInstrument(int instrumentID, java.util.Date startDate, java.util.Date endDate, 
			boolean truncate, String sortBy) throws SQLException {
	    // Get our connection to the database.
        Connection conn = null;
        try {
            conn = DBConnectionManager.getConnection("pr");
            return getUsageBlocksForInstrument(instrumentID, startDate, endDate, truncate, sortBy, conn);
            
        } 
        finally {
            // Make sure the connection is returned to the pool
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { ; }
                conn = null;
            }
        }
	}
	
	List<UsageBlock> getUsageBlocksForInstrument(int instrumentID, 
	        java.util.Date startDate, java.util.Date endDate, boolean truncate,
	        String sortBy, Connection conn) throws SQLException {
        if (conn == null)
            return null;
        List <UsageBlock> usageBlks = new ArrayList<UsageBlock>();
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            String sql = makeUsageSql(instrumentID, startDate, endDate, sortBy);
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                
                UsageBlock newBlk = makeUsageBlock(rs);
                
                // Truncate the actual usage time, if required, to match the given start and end dates.
                if(truncate) {
                	truncateBlock(newBlk, startDate, endDate);
                }
                usageBlks.add(newBlk);
            }
            return usageBlks;
            
        } 
        
        finally {
            // Always make sure result sets and statements are closed,
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { ; }
                rs = null;
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { ; }
                stmt = null;
            }
        }
    }
	
    private UsageBlock makeUsageBlock(ResultSet rs) throws SQLException {
        UsageBlock blk = new UsageBlock();
        blk.setID(rs.getInt("id"));
        blk.setResearcherID(rs.getInt("enteredBy"));
        blk.setInstrumentID(rs.getInt("instrumentID"));
        blk.setInstrumentRateID(rs.getInt("instrumentRateID"));
        blk.setInstrumentName(rs.getString("name"));
        blk.setProjectID(rs.getInt("projectID"));
        blk.setProjectTitle(rs.getString("projectTitle"));
        blk.setPIID(rs.getInt("projectPI"));
        blk.setProjectPI(rs.getString("researcherLastName"));
        blk.setStartDate(rs.getTimestamp("startDate"));
        blk.setEndDate(rs.getTimestamp("endDate"));
        blk.setDateCreated(rs.getTimestamp("dateEntered"));
        blk.setDateChanged(rs.getTimestamp("lastChanged"));
        blk.setNotes(rs.getString("notes"));
        
        InstrumentUsagePaymentDAO iupDao = InstrumentUsagePaymentDAO.getInstance();
        List<InstrumentUsagePayment> payments = iupDao.getPaymentsForUsage(blk.getID());
        blk.setPayments(payments);
        
        InstrumentRateDAO rateDao = InstrumentRateDAO.getInstance();
        InstrumentRate rate = rateDao.getInstrumentRate(blk.getInstrumentRateID());
        if(rate == null)
        	throw new SQLException("No instrument rate found for ID: "+blk.getInstrumentRateID());
        blk.setRate(rate.getRate());
        
        return blk;
    }
	
    
    //--------------------------------------------------------------------------------------------
    // Project instrument usage
    //--------------------------------------------------------------------------------------------
    public List <ProjectInstrumentUsage> getAllProjectInstrumentUsage(java.util.Date startDate, java.util.Date endDate) throws SQLException {
        
        List<UsageBlock> usageBlocks = getAllUsageBlocks(startDate, endDate);
        
        // sort by projectID and then instrumentID
        Collections.sort(usageBlocks, new Comparator<UsageBlock>() {
            public int compare(UsageBlock o1, UsageBlock o2) {
                if(o1.getProjectID() < o2.getProjectID())   return -1;
                if(o1.getProjectID() > o2.getProjectID())   return 1;
                if(o1.getInstrumentID() < o2.getInstrumentID()) return -1;
                if(o1.getInstrumentID() > o2.getInstrumentID()) return 1;
                return 0;
            }});
        
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
    
	//--------------------------------------------------------------------------------------------
	// MS instrument usage
	//--------------------------------------------------------------------------------------------
	public List <MsInstrumentUsage> getAllInstrumentUsage(Date startDate, Date endDate) throws SQLException {
		// get the MS instruments
		List <MsInstrument> instruments = getMsInstruments(); // get active and retired instruments
		
		if (instruments == null)
			return null;
		
		Collections.sort(instruments, new Comparator<MsInstrument>() {
			@Override
			public int compare(MsInstrument o1, MsInstrument o2) {
				if (o1.getID() < o2.getID())	return -1;
				if (o1.getID() > o2.getID())	return 1;
				return 0;
			}});
		
		List <MsInstrumentUsage> summaries = new ArrayList<MsInstrumentUsage>(instruments.size());
		for (MsInstrument instrument: instruments) {
			MsInstrumentUsage summ = getInstrumentUsage(instrument.getID(), startDate, endDate);
			summaries.add(summ);
		}
		return summaries;
	}
	
	public MsInstrumentUsage getInstrumentUsage(int instrumentID, java.util.Date startDate, java.util.Date endDate) throws SQLException {
		
		MsInstrument instrument = getMsInstrument(instrumentID);
		if (instrument == null) {
			return null;
		}
		
		
		// Get our connection to the database.
		Connection conn = null;
		try {
			conn = DBConnectionManager.getConnection("pr");
			return getInsrumentUsage(instrumentID, startDate, endDate, conn);
			
		} 
		finally {
			// Make sure the connection is returned to the pool
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
	}
	
	MsInstrumentUsage getInsrumentUsage(int instrumentID, java.util.Date startDate, java.util.Date endDate, Connection conn) throws SQLException {
		
		if (conn == null)
			return null;
		
		MsInstrument instrument = getMsInstrument(instrumentID, conn);
		if (instrument == null) {
			return null;
		}
		
		List<UsageBlock> usageBlocks = getUsageBlocksForInstrument(instrumentID, startDate, endDate, 
																   true, // truncate blocks to given start and end dates 
																   "projectID", conn);
		
		List <ProjectInstrumentUsage> projUsageList = new ArrayList<ProjectInstrumentUsage>();
		
		ProjectInstrumentUsage projUsage = null;
		for(UsageBlock usageBlock: usageBlocks) {
			
			   
		    if (projUsage == null || projUsage.getProjectID() != usageBlock.getProjectID()) {
		        projUsage = new ProjectInstrumentUsage(usageBlock.getProjectID(),
		                        usageBlock.getProjectTitle(), 
		                        usageBlock.getPIID(),
		                        usageBlock.getProjectPI(),
		                        usageBlock.getInstrumentID(),
		                        usageBlock.getInstrumentName(),
		                        new Timestamp(startDate.getTime()), 
		                        new Timestamp(endDate.getTime()));
		        projUsageList.add(projUsage);
		    }
		    
		    projUsage.addUsageBlock(usageBlock);
		}
		
		MsInstrumentUsage summary = new MsInstrumentUsage(instrumentID, instrument.getName(),
				                                          instrument.isActive(),
				                                          new Timestamp(startDate.getTime()), 
				                                          new Timestamp(endDate.getTime()));
		summary.setProjectUsageList(projUsageList);
		return summary;
	}
	
	//--------------------------------------------------------------------------------------------
	// InstrumentCalendar
	//--------------------------------------------------------------------------------------------
	public InstrumentCalendar getInstrumentCalendar(int month, int year, int instrumentID) 
	        throws InvalidProjectTypeException, InvalidIDException {

		// Get our connection to the database.
		Connection conn = null;
		try {
			conn = DBConnectionManager.getConnection("pr");
			return getInstrumentCalendar(month, year, instrumentID, conn);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			// Make sure the connection is returned to the pool
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
		return null;
	}
	
	public InstrumentCalendar getInstrumentCalendar(int month, int year, int instrumentID, Connection conn) 
	        throws InvalidProjectTypeException, InvalidIDException {
		
		if (conn == null)
			return null;
		
		MsInstrument instrument = getMsInstrument(instrumentID, conn);
		if (instrument == null) {
			return null;
		}
		
		Date startDate = DateUtils.getDate(1, month, year);
		Date endDate = DateUtils.getDate(31, month, year, true); // if the month has < 31 days, DateUtils will correct for that
//		System.out.println("start date: "+startDate);
//		System.out.println("end date: "+endDate);
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		InstrumentCalendar calendar = new InstrumentCalendar(year, month, instrument.getName(), instrumentID);
		
		try {
			String sql = makeUsageSql(instrumentID, startDate, endDate, "projectID");
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				int projectID = rs.getInt("projectID");
				Date start = rs.getDate("startDate");
				start = start.getTime() > startDate.getTime() ? start : startDate;
				Date end = rs.getDate("endDate");
				end = end.getTime() < endDate.getTime() ? end : endDate;
				addToCalendar(calendar, start, end, projectID);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		finally {
			// Always make sure result sets and statements are closed,
			if (rs != null) {
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
		}
		return calendar;
	}
	
	// start and end dates should fall in the same month!
	private void addToCalendar(InstrumentCalendar calendar, Date start, Date end, int projectID) 
	        throws InvalidProjectTypeException, SQLException, InvalidIDException {
		int startDay = DateUtils.getDay(start);
		int endDay = DateUtils.getDay(end);
		for (int i = startDay; i <= endDay; i++) {
			calendar.addBusyDay(i, projectID);
		}
	}

	//--------------------------------------------------------------------------------------------
	// SQL
	//--------------------------------------------------------------------------------------------
	private String makeUsageSql(int usageId) {
        StringBuilder buf = new StringBuilder();
        buf.append(baseSql());
        buf.append("AND insUsg.id="+usageId);
        System.out.println(buf.toString());
        return buf.toString();
    }
	
	private String makeUsageSql(int instrumentID, java.util.Date startDate, java.util.Date endDate, String orderBy) {
		StringBuilder buf = new StringBuilder();
		buf.append(baseSql());
		if (instrumentID != -1) {
			buf.append("AND instrumentID=");
			buf.append(instrumentID);
			buf.append(" ");
		}
		if(startDate != null) {
		    buf.append("AND startDate <= ");
		    buf.append(makeDateForQuery(endDate));
		    buf.append(" ");
		}
		if(endDate != null) {
		    buf.append("AND endDate >= ");
		    buf.append(makeDateForQuery(startDate));
		    buf.append(" ");
		}
		if (orderBy != null)
			buf.append("ORDER BY "+orderBy);
		
//		System.out.println(buf.toString());
		return buf.toString();
	}
	
	private String baseSql() {
	    StringBuilder buf = new StringBuilder();
        buf.append("SELECT insUsg.*, "+
                "ins.name, "
                +"proj.projectTitle, proj.projectPI, "+
                "r.researcherLastName "
                 );
        buf.append("FROM instruments AS ins, instrumentUsage AS insUsg, tblProjects AS proj, tblResearchers AS r ");
        buf.append("WHERE proj.projectID=insUsg.projectID ");
        buf.append("AND r.researcherID=proj.projectPI ");
        buf.append("AND ins.id=insUsg.instrumentID ");
        return buf.toString();
	}
	
	private String makeDateForQuery(java.util.Date date) {
//		StringBuilder buf = new StringBuilder("DATE('"+DateUtils.getYear(date)+"-"+DateUtils.getMonth(date)+"-"+DateUtils.getDay(date));
//		buf.append(" "+DateUtils.getHour24(date)+":"+DateUtils.getMinutes(date)+":"+DateUtils.getSeconds(date)+"')");
	    
	    StringBuilder buf = new StringBuilder("'"+DateUtils.getYear(date)+"-"+DateUtils.getMonth(date)+"-"+DateUtils.getDay(date));
	    buf.append(" "+DateUtils.getHour24(date)+":"+DateUtils.getMinutes(date)+":"+DateUtils.getSeconds(date)+"'");
		
		return buf.toString();
	}
}

