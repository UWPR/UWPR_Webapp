package org.uwpr.instrumentlog;

import org.yeastrc.data.InvalidIDException;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.InvalidProjectTypeException;

import java.sql.*;
import java.util.*;
import java.util.Date;

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
	public List <MsInstrument> getMsInstruments()
	{
		List<MsInstrument> instruments = getMsInstruments(false);
		Collections.sort(instruments, new Comparator<MsInstrument>()
		{
			@Override
			public int compare(MsInstrument o1, MsInstrument o2)
			{
				if((o1.isActive() && o2.isActive()) || (!o1.isActive() && !o2.isActive()))
				{
					return Integer.valueOf(o1.getID()).compareTo(o2.getID());
				}
				return o1.isActive() ? -1 : 1;
			}
		});
		return instruments;
	}
	
	public List <MsInstrument> getMsInstruments(boolean activeOnly) {
		
		// Get our connection to the database.
		Connection conn = null;
		
		try {
			conn = getConnection();
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
			String sql = "select * from " + DBConnectionManager.getInstrumentsTableSQL();
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
			conn = getConnection();
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
	
	public MsInstrument getMsInstrument(int instrumentID, Connection conn) {
		
		if (conn == null)
			return null;
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "select * from " + DBConnectionManager.getInstrumentsTableSQL() + " where id="+instrumentID;
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
			conn = getConnection();
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
	
	private MsInstrumentUsage getInsrumentUsage(int instrumentID, java.util.Date startDate, java.util.Date endDate, Connection conn) throws SQLException {
		
		if (conn == null)
			return null;
		
		MsInstrument instrument = getMsInstrument(instrumentID, conn);
		if (instrument == null) {
			return null;
		}
		
		List<UsageBlock> usageBlocks = UsageBlockDAO.getUsageBlocksForInstrument(instrumentID, startDate, endDate,
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
			conn = getConnection();
			return getInstrumentCalendar(month, year, instrumentID, conn);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			// Make sure the connection is returned to the pool
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
			}
		}
		return null;
	}

    private Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getMainDbConnection();
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
			UsageBlockFilter filter = new UsageBlockFilter();
			filter.setInstrumentId(instrumentID);
			filter.setStartDate(startDate);
			filter.setEndDate(endDate);
			filter.setSortColumn("projectID");
			String sql = UsageBlockBaseDAO.makeSql(filter);
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
}

