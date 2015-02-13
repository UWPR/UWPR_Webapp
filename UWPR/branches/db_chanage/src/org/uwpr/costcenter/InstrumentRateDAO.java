/**
 * InstrumentRateDAO.java
 * @author Vagisha Sharma
 * Apr 29, 2011
 */
package org.uwpr.costcenter;

import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.yeastrc.db.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class InstrumentRateDAO {

	private InstrumentRateDAO() {}
	
	private static final InstrumentRateDAO instance = new InstrumentRateDAO();
	
	public static InstrumentRateDAO getInstance() {
		return instance;
	}
	
	public List<InstrumentRate> getAllRates() throws SQLException {
		
		return getRates(0, 0, 0, /*all rates*/ -1);
	}
	
	public List<InstrumentRate> getCurrentRates() throws SQLException {
		
		return getRates(0, 0, 0, /*current rates*/ 1);
	}

	public List<InstrumentRate> getRates(Connection conn, int instrumentId, int rateTypeId, int timeBlockId, int current) throws SQLException {
		
		StringBuilder sql = new StringBuilder("SELECT * FROM instrumentRate ");
		if(instrumentId != 0 || rateTypeId != 0 || timeBlockId != 0 || current != -1) {
			
			sql.append("WHERE");
			boolean first = true;
			
			if(instrumentId != 0) {
				sql.append (" instrumentID="+instrumentId);
				first = false;
			}
			
			if(rateTypeId != 0) {
				if(!first)
					sql.append(" AND");
				sql.append(" rateTypeID="+rateTypeId);
				first = false;
			}
			
			if(timeBlockId != 0) {
				if(!first)
					sql.append(" AND");
				sql.append(" blockID="+timeBlockId);
				first = false;
			}
			
			if(current != -1) {
				if(!first)
					sql.append(" AND");
				sql.append(" isCurrent="+current);
			}
		}
		sql.append(" ORDER BY instrumentID, blockID, rateTypeID, id");
		
		Statement stmt = null;
		ResultSet rs = null;
		
		List<InstrumentRate> rates = new ArrayList<InstrumentRate>();
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			
			while(rs.next()) {
				
				InstrumentRate rate = makeInstrumentRate(conn, rs);
				rates.add(rate);
			}
		}
		finally {
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		return rates;
	}

    public List<InstrumentRate> getRates(int instrumentId, int rateTypeId, int timeBlockId, int current) throws SQLException {

        Connection conn = null;

        try
        {
            conn = DBConnectionManager.getMainDbConnection();
            return getRates(conn, instrumentId, rateTypeId, timeBlockId, current);
        }
        finally
        {
            if(conn != null) try {conn.close();} catch(SQLException e){}
        }
    }


	private InstrumentRate makeInstrumentRate(Connection conn, ResultSet rs) throws SQLException {
		InstrumentRate rate = new InstrumentRate();
		rate.setCreateDate(rs.getTimestamp("createDate"));
		rate.setId(rs.getInt("id"));
		rate.setRate(rs.getBigDecimal("fee"));
		rate.setCurrent(rs.getBoolean("isCurrent"));

		int instrumentId = rs.getInt("instrumentID");
		rate.setInstrument(MsInstrumentUtils.instance().getMsInstrument(instrumentId, conn));
		
		int timeBlockId = rs.getInt("blockID");
		rate.setTimeBlock(TimeBlockDAO.getInstance().getTimeBlock(timeBlockId, conn));
		
		int rateTypeId = rs.getInt("rateTypeID");
		rate.setRateType(RateTypeDAO.getInstance().getRateType(rateTypeId, conn));
		return rate;
	}
	
	public List<InstrumentRate> getAllCurrentRatesForInstrument(int instrumentId) throws SQLException {
		
		String sql = "SELECT * FROM instrumentRate WHERE instrumentID = "+instrumentId+" AND isCurrent=1";
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		List<InstrumentRate> rates = new ArrayList<InstrumentRate>();
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				
				InstrumentRate rate = makeInstrumentRate(conn, rs);
				rates.add(rate);
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		return rates;
	}
	
	
	public boolean hasRatesForTimeBlock(int timeBlockId) throws SQLException {
		
		String sql = "SELECT count(*) FROM instrumentRate WHERE blockID = "+timeBlockId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()) {
				int count = rs.getInt(1);
				if(count == 0)
					return false;
				else
					return true;
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		return false;
	}

    public static final String saveInstrumentRateSql = "INSERT INTO instrumentRate (instrumentID, blockID, rateTypeID, fee, createDate, isCurrent) VALUES (?,?,?,?,?,?)";

    public void saveInstrumentRate(InstrumentRate rate) throws SQLException {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.prepareStatement(saveInstrumentRateSql);
			saveInstrumentRate(rate, stmt);
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}

    public int saveInstrumentRate(InstrumentRate rate, PreparedStatement stmt) throws SQLException {

        stmt.setInt(1, rate.getInstrument().getID());
        stmt.setInt(2, rate.getTimeBlock().getId());
        stmt.setInt(3, rate.getRateType().getId());
        stmt.setBigDecimal(4, rate.getRate());
        stmt.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
        stmt.setBoolean(6, rate.isCurrent());

        return stmt.executeUpdate();

    }
	
	public void updateInstrumentRate(InstrumentRate rate) throws SQLException {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			
			String sql = "SELECT * FROM instrumentRate WHERE id="+rate.getId();
			rs = stmt.executeQuery(sql);
			
			if(!rs.next()) {
				throw new SQLException("No instrument rate found for id: "+rate.getId());
			}
			
			rs.updateInt("instrumentID", rate.getInstrument().getID());
			rs.updateInt("blockID", rate.getTimeBlock().getId());
			rs.updateInt("rateTypeID", rate.getRateType().getId());
			rs.updateBigDecimal("fee", rate.getRate());
			rs.updateBoolean("isCurrent", rate.isCurrent());
			// From mySQL docs (http://dev.mysql.com/doc/refman/5.1/en/timestamp.html)
			// The auto-update TIMESTAMP column, if there is one, is automatically updated to the current timestamp 
			// when the value of any other column in the row is changed from its current value, 
			// unless the TIMESTAMP column explicitly is assigned a value other than NULL. 
			rs.updateNull("lastUpdate"); // explicitly set to null so that it automatically updates
			
			rs.updateRow();
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}
	
	
	
	public void deleteInstrumentRate(int instrumentRateId) throws SQLException {
		
		// NOTE: There is a trigger on instrumentRate table that will 
		//       delete all entries in the instrumentUsage where instrumentRateID is equal to 
		//       the given instrumentRateID
		
		String sql = "DELETE FROM instrumentRate where id="+instrumentRateId;
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}
		
	public InstrumentRate getInstrumentCurrentRate(int instrumentId, int timeBlockId, int rateTypeId) throws SQLException {
		
		String sql = "SELECT * FROM instrumentRate WHERE instrumentID="+instrumentId+
		             " AND blockID="+timeBlockId+" AND rateTypeID="+rateTypeId+
		             " AND isCurrent=1";
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		InstrumentRate rate = null;
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()) {
				
				rate = makeInstrumentRate(conn, rs);
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		return rate;
	}
	
	public List<InstrumentRate> getInstrumentCurrentRates(int instrumentId, int rateTypeId) throws SQLException {
		
		String sql = "SELECT * FROM instrumentRate WHERE instrumentID="+instrumentId+
		             " AND rateTypeID="+rateTypeId+" AND isCurrent=1";
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		List<InstrumentRate> rates = new ArrayList<InstrumentRate>();
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				InstrumentRate rate = makeInstrumentRate(conn, rs);
				rates.add(rate);
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		return rates;
	}
	
	public InstrumentRate getInstrumentRate(int instrumentRateId) throws SQLException {
		
		String sql = "SELECT * FROM instrumentRate WHERE id="+instrumentRateId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		InstrumentRate rate = null;
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()) {
				
				rate = makeInstrumentRate(conn, rs);
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		return rate;
	}
		
}
