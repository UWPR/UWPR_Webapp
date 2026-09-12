/**
 * InvoiceInstrumentUsageDAO.java
 * @author Vagisha Sharma
 * Jul 16, 2011
 */
package org.uwpr.costcenter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.sql.*;
import java.util.List;

/**
 * 
 */
public class InvoiceInstrumentUsageDAO {

	private static final Logger log = LogManager.getLogger(InvoiceInstrumentUsageDAO.class);
	
	private static InvoiceInstrumentUsageDAO instance = new InvoiceInstrumentUsageDAO();
	
	public static InvoiceInstrumentUsageDAO getInstance() {
		return instance;
	}
	
	public void saveBlocks(Connection conn, List<InvoiceInstrumentUsage> invoiceBlocks) throws SQLException {
		
		String sql = "INSERT INTO invoiceInstrumentUsage (invoiceID, instrumentUsageID) VALUES (?,?)";
		PreparedStatement stmt = null;
		
		try {

			stmt = conn.prepareStatement(sql);
			for(InvoiceInstrumentUsage invoiceBlock: invoiceBlocks) {

				stmt.setInt(1, invoiceBlock.getInvoiceId());
				stmt.setInt(2, invoiceBlock.getInstrumentUsageId());
				stmt.executeUpdate();
			}
		}
		finally {
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}
	
	public InvoiceInstrumentUsage getInvoiceBlock (int instrumentUsageId) throws SQLException {

		// Inner-join invoice so a link left behind by a deleted invoice does not report the block
		// as billed.  Callers treat a non-null result as "already invoiced" and refuse to edit it.
		String sql = "SELECT iiu.* FROM invoiceInstrumentUsage iiu"
				+ " INNER JOIN invoice i ON i.id = iiu.invoiceID"
				+ " WHERE iiu.instrumentUsageID="+instrumentUsageId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next())
			{
				InvoiceInstrumentUsage invoiceBlock = new InvoiceInstrumentUsage();
				invoiceBlock.setId(rs.getInt("id"));
				invoiceBlock.setInvoiceId(rs.getInt("invoiceID"));
				invoiceBlock.setInstrumentUsageId(rs.getInt("instrumentUsageID"));
				return invoiceBlock;
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}

		return null;
	}

	public void deleteBlocksForInvoice (Connection conn, int invoiceId) throws SQLException {

		String sql = "DELETE FROM invoiceInstrumentUsage WHERE invoiceID="+invoiceId;
		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
		}
		finally {
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}

	public void deleteBlocksForUsage (Connection conn, int instrumentUsageId) throws SQLException {

		String sql = "DELETE FROM invoiceInstrumentUsage WHERE instrumentUsageID="+instrumentUsageId;
		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
		}
		finally {
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}
}
