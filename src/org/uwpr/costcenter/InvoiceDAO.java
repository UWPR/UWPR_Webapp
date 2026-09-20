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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 */
public class InvoiceDAO {

	private static final Logger log = LogManager.getLogger(InvoiceDAO.class);
	
	private static InvoiceDAO instance = new InvoiceDAO();
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	
	public static InvoiceDAO getInstance() {
		return instance;
	}
	
	public void save(Invoice invoice) throws SQLException {
		
		String sql = "INSERT INTO invoice (billStartDate, billEndDate, createdBy) VALUES (?,?,?)";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setTimestamp(1, new Timestamp(invoice.getBillStartDate().getTime()));
			stmt.setTimestamp(2, new Timestamp(invoice.getBillEndDate().getTime()));
			stmt.setInt(3, invoice.getCreatedBy());
			stmt.executeUpdate();
			
			rs = stmt.getGeneratedKeys(); 

			if ( rs != null && rs.next() ) {
				invoice.setId( rs.getInt(1) );
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}
	
	public Invoice getInvoice (Date startDate, Date endDate) throws SQLException {
		
		String sql = "SELECT * FROM invoice WHERE billStartDate='"+dateFormat.format(startDate)+"'"
		+ "AND billEndDate='"+dateFormat.format(endDate)+"'";
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()) {
				Invoice invoice = new Invoice();
				invoice.setId(rs.getInt("id"));
				invoice.setCreateDate(rs.getTimestamp("createDate"));
				invoice.setBillStartDate(rs.getTimestamp("billStartDate"));
				invoice.setBillEndDate(rs.getTimestamp("billEndDate"));
				invoice.setCreatedBy(rs.getInt("createdBy"));
				return invoice;
			}
			else {
				log.error("No entry found in table invoice for startDate: "+startDate.toString()+" and endDate: "+endDate.toString());
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		return null;
	}
	
	public void delete(Invoice invoice) throws SQLException {

		String sql = "DELETE FROM invoice WHERE id="+invoice.getId();
		Connection conn = null;
		Statement stmt = null;
		boolean committed = false;

		try {
			conn = DBConnectionManager.getMainDbConnection();
			conn.setAutoCommit(false);

			// Delete the invoice's usage links, then the invoice, on one connection.  No trigger
			// removes the links, and a link left behind reports its block as billed.  Both deletes
			// share this transaction, so a failure on either rolls back the other and leaves the
			// invoice re-deletable.  That holds once invoice is InnoDB.  Until the migration runs it is
			// MyISAM and commits on its own, so a later failure still strands the links.
			InvoiceInstrumentUsageDAO.getInstance().deleteBlocksForInvoice(conn, invoice.getId());

			stmt = conn.createStatement();
			stmt.execute(sql);

			conn.commit();
			committed = true;
		}
		finally {
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			DBConnectionManager.endTransactionAndClose(conn, committed);
		}
	}
}
