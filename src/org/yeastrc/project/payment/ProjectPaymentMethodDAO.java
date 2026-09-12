/**
 * ProjectPaymentMethodDAO.java
 * @author Vagisha Sharma
 * May 20, 2011
 */
package org.yeastrc.project.payment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uwpr.instrumentlog.InstrumentUsagePaymentDAO;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.ProjectMismatchException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 */
public class ProjectPaymentMethodDAO {

	private ProjectPaymentMethodDAO() {}
	
	private static ProjectPaymentMethodDAO instance = new ProjectPaymentMethodDAO();
	
	private static final Logger log = LogManager.getLogger(ProjectPaymentMethodDAO.class);
	
	public static ProjectPaymentMethodDAO getInstance() {
		return instance;
	}
	
	/**
	 * True if the payment method is linked to the project.
	 *
	 * Actions take a paymentMethodId and a projectId as separate request parameters, so the
	 * two have to be checked against each other before the project's access or archived state
	 * says anything about the payment method.
	 */
	public boolean belongsToProject(int paymentMethodId, int projectId) throws SQLException {

		String sql = "SELECT COUNT(*) FROM projectPaymentMethod WHERE paymentMethodID=? AND projectID=?";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = getConnection();

			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, paymentMethodId);
			stmt.setInt(2, projectId);
			rs = stmt.executeQuery();
			return rs.next() && rs.getInt(1) > 0;
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

	/**
	 * Loads the payment method for a project, tying the load and the ownership check together so a
	 * caller cannot do one without the other.  Returns null if the payment method does not exist, and
	 * throws ProjectMismatchException if it exists but is not linked to the project, so
	 * the caller can tell a not-found from a not-associated and report each.
	 */
	public PaymentMethod getPaymentMethodForProject(int paymentMethodId, int projectId)
			throws SQLException, ProjectMismatchException {

		PaymentMethod paymentMethod = PaymentMethodDAO.getInstance().getPaymentMethod(paymentMethodId);
		if(paymentMethod == null) {
			return null;
		}
		if(!belongsToProject(paymentMethodId, projectId)) {
			throw new ProjectMismatchException(
					"Payment method "+paymentMethodId+" is not associated with project "+projectId+".");
		}
		return paymentMethod;
	}

	public List<PaymentMethod> getPaymentMethods(int projectId) throws SQLException {
		
		List<Integer> paymentMethodIds = new ArrayList<Integer>();
		String sql = "SELECT paymentMethodID FROM projectPaymentMethod WHERE projectID="+projectId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next()) {
				paymentMethodIds.add(rs.getInt("paymentMethodID"));
			}
			
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		List<PaymentMethod> paymentMethodList = new ArrayList<PaymentMethod>(paymentMethodIds.size());
		
		PaymentMethodDAO pmdao = PaymentMethodDAO.getInstance();
		for(Integer paymentMethodId: paymentMethodIds) {
			
			PaymentMethod pm = pmdao.getPaymentMethod(paymentMethodId);
			if(pm != null) {
				paymentMethodList.add(pm);
			}
		}
		
		return paymentMethodList;
	}
	
	public List<PaymentMethod> getCurrentPaymentMethods(int projectId) throws SQLException {
		
		List<Integer> paymentMethodIds = new ArrayList<Integer>();
		String sql = "SELECT paymentMethodID FROM projectPaymentMethod WHERE projectID="+projectId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next()) {
				paymentMethodIds.add(rs.getInt("paymentMethodID"));
			}
			
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		List<PaymentMethod> paymentMethodList = new ArrayList<PaymentMethod>(paymentMethodIds.size());
		
		PaymentMethodDAO pmdao = PaymentMethodDAO.getInstance();
		for(Integer paymentMethodId: paymentMethodIds) {
			
			PaymentMethod pm = pmdao.getPaymentMethod(paymentMethodId);
			if(pm != null && pm.isCurrent()) {
				paymentMethodList.add(pm);
			}
		}
		
		return paymentMethodList;
	}
	
	public void savePaymentMethod(int projectId, PaymentMethod paymentMethod) throws SQLException {

		Connection conn = null;

		String sql = "INSERT INTO projectPaymentMethod (projectID, paymentMethodID) VALUES (?,?)";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			conn.setAutoCommit(false);

			// First save the payment method
			int paymentMethodId = PaymentMethodDAO.getInstance().savePaymentMethod(conn, paymentMethod);

			// Now create an entry in the bridge table
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, projectId);
			stmt.setInt(2, paymentMethodId);
			
			int numRowsInserted = stmt.executeUpdate();
			if(numRowsInserted == 0) {
				throw new SQLException("Creating project payment method failed, no rows affected.");
			}

			conn.commit();
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}
	
	public void deletePaymentMethod(int paymentMethodId) throws SQLException {

		// No trigger cleans up the child rows, and PaymentMethodDAO.deletePaymentMethod opens its own
		// connection, so this is not one transaction.  Delete the children before the payment method,
		// so a failure part way leaves a harmless extra child row rather than an orphaned link.

		// The bridge rows linking the method to its projects.  These are the 6 orphaned
		// projectPaymentMethod rows on prod, from this line having been commented out.
		unlinkProjectPaymentMethod(paymentMethodId, 0);

		// Any instrumentUsagePayment splits.  DeletePaymentMethodAction refuses a method still in
		// use, but a split whose usage block is gone now passes that check, so clear it here.
		Connection conn = null;
		try {
			conn = getConnection();
			InstrumentUsagePaymentDAO.getInstance().deletePaymentsForPaymentMethod(conn, paymentMethodId);
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
		}

		// Finally the payment method itself.
		PaymentMethodDAO.getInstance().deletePaymentMethod(paymentMethodId);
	}

	public void unlinkProjectPaymentMethod(int paymentMethodId, int projectId) throws SQLException {
		
		if(paymentMethodId == 0 && projectId == 0) {
			log.error("paymentMethodId and projectId are both 0 in unlinkProjectPaymentMethod. Skipping...");
			return;
		}
		
		String sql = "DELETE FROM projectPaymentMethod WHERE ";
		if(paymentMethodId != 0) {
			sql += "paymentMethodID="+paymentMethodId;
		}
		if(paymentMethodId != 0 && projectId != 0)
			sql += " AND ";
		if(projectId != 0) {
			sql += " projectID="+projectId;
		}
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
            stmt.executeUpdate(sql);
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

    private Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getMainDbConnection();
    }
}
