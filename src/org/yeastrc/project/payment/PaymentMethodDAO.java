/**
 * PaymentMethodDAO.java
 * @author Vagisha Sharma
 * May 20, 2011
 */
package org.yeastrc.project.payment;

import org.apache.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 */
public class PaymentMethodDAO {

	private PaymentMethodDAO() {}
	
	private static final Logger log = Logger.getLogger(PaymentMethodDAO.class);
	
	private static PaymentMethodDAO instance = new PaymentMethodDAO();
	
	public static PaymentMethodDAO getInstance() {
		return instance;
	}
	
	public PaymentMethod getPaymentMethod (int paymentMethodId) throws SQLException {
		
		String sql = "SELECT * FROM paymentMethod WHERE id="+paymentMethodId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()) {
                return makePaymentMethod(rs);
			}
			else {
				log.error("No entry found in table isCurrent for id: "+paymentMethodId);
				return null;
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException ignored){}
			if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
			if(rs != null) try {rs.close();} catch(SQLException ignored){}
		}
	}

    private PaymentMethod makePaymentMethod(ResultSet rs) throws SQLException {

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(rs.getInt("id"));
        paymentMethod.setUwbudgetNumber(rs.getString("UWBudgetNumber"));
        paymentMethod.setPonumber(rs.getString("PONumber"));
        paymentMethod.setContactFirstName(rs.getString("contactNameFirst"));
        paymentMethod.setContactLastName(rs.getString("contactLastName"));
        paymentMethod.setContactEmail(rs.getString("contactEmail"));
        paymentMethod.setContactPhone(rs.getString("contactPhone"));
        paymentMethod.setOrganization(rs.getString("organization"));
        paymentMethod.setAddressLine1(rs.getString("addressLine1"));
        paymentMethod.setAddressLine2(rs.getString("addressLine2"));
        paymentMethod.setCity(rs.getString("city"));
        paymentMethod.setState(rs.getString("state"));
        paymentMethod.setZip(rs.getString("zip"));
        paymentMethod.setCountry(rs.getString("country"));
        paymentMethod.setCreatorId(rs.getInt("createdBy"));
        paymentMethod.setCreateDate(rs.getTimestamp("dateCreated"));
        paymentMethod.setLastUpdateDate(rs.getTimestamp("lastUpdated"));
        paymentMethod.setCurrent(rs.getBoolean("isCurrent"));
        paymentMethod.setFederalFunding(rs.getBoolean("federalFunding"));
        paymentMethod.setPoAmount(rs.getBigDecimal("POAmount"));

        paymentMethod.setTotalCost(getCost(paymentMethod.getId()));
        paymentMethod.setInvoicedCost(getInvoicedCost(paymentMethod.getId()));

        return paymentMethod;
    }

    public List<PaymentMethod> getPaymentMethods (String contactFirstName, String contactLastName) throws SQLException {
		
		String sql = "SELECT * FROM paymentMethod WHERE contactNameFirst=\""+contactFirstName+"\" AND contactLastName=\""+contactLastName+"\"";
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			List<PaymentMethod> methods = new ArrayList<PaymentMethod>();
			
			while(rs.next()) {
                methods.add(makePaymentMethod(rs));
			}
			
			return methods;
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException ignored){}
			if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
			if(rs != null) try {rs.close();} catch(SQLException ignored){}
		}
	}

	
	public int savePaymentMethod(PaymentMethod paymentMethod) throws SQLException {
		
		String sql = "INSERT INTO paymentMethod (UWBudgetNumber, PONumber, contactNameFirst, contactLastName, contactEmail,";
		sql += " contactPhone, organization, addressLine1, addressLine2, city, state, zip, country, ";
		sql += " dateCreated,  createdBy, isCurrent, federalFunding, POAmount) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, paymentMethod.getUwbudgetNumber());
			stmt.setString(2, paymentMethod.getPonumber());
			stmt.setString(3, paymentMethod.getContactFirstName());
			stmt.setString(4, paymentMethod.getContactLastName());
			stmt.setString(5, paymentMethod.getContactEmail());
			stmt.setString(6, paymentMethod.getContactPhone());
			stmt.setString(7, paymentMethod.getOrganization());
			stmt.setString(8, paymentMethod.getAddressLine1());
			stmt.setString(9, paymentMethod.getAddressLine2());
			stmt.setString(10, paymentMethod.getCity());
			stmt.setString(11, paymentMethod.getState());
			stmt.setString(12, paymentMethod.getZip());
			stmt.setString(13, paymentMethod.getCountry());
			stmt.setTimestamp(14, new Timestamp(new Date().getTime()));
			stmt.setInt(15, paymentMethod.getCreatorId());
			stmt.setInt(16, 1);
			if(paymentMethod.isFederalFunding()) {
				stmt.setInt(17, 1);
			}
			else {
				stmt.setInt(17, 0);
			}
			stmt.setBigDecimal(18, paymentMethod.getPoAmount());

			int numRowsInserted = stmt.executeUpdate();
			if(numRowsInserted == 0) {
				throw new SQLException("Creating payment method failed, no rows affected.");
			}
			
			rs = stmt.getGeneratedKeys();
	        if (rs.next()) {
	        	paymentMethod.setId(rs.getInt(1));
	        } else {
	            throw new SQLException("Creating payment method failed, no generated key obtained.");
	        }

			return paymentMethod.getId();
			
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException ignored){}
			if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
			if(rs != null) try {rs.close();} catch(SQLException ignored){}
		}
	}
	
	public void updatePaymentMethod(PaymentMethod paymentMethod) throws SQLException {
		
		String sql = "UPDATE paymentMethod ";
		sql += "SET UWBudgetNumber = ?";
		sql += ", PONumber = ?";
		sql += ", contactNameFirst= ?";
		sql += ", contactLastName = ?";
		sql += ", contactEmail = ?";
		sql += ", contactPhone = ?";
		sql += ", organization = ?";
		sql += ", addressLine1 = ?";
		sql += ", addressLine2 = ?";
		sql += ", city = ?";
		sql += ", state = ?";
		sql += ", zip = ?";
		sql += ", country = ?";
		sql += ", createdBy = ?";
		sql += ", isCurrent = ?";
		sql += ", federalFunding = ?";
        sql += ", POAmount = ?";
		sql += " WHERE id=?";
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			stmt = conn.prepareStatement(sql);
			
			stmt.setString(1, paymentMethod.getUwbudgetNumber());
			stmt.setString(2, paymentMethod.getPonumber());
			stmt.setString(3, paymentMethod.getContactFirstName());
			stmt.setString(4, paymentMethod.getContactLastName());
			stmt.setString(5, paymentMethod.getContactEmail());
			stmt.setString(6, paymentMethod.getContactPhone());
			stmt.setString(7, paymentMethod.getOrganization());
			stmt.setString(8, paymentMethod.getAddressLine1());
			stmt.setString(9, paymentMethod.getAddressLine2());
			stmt.setString(10, paymentMethod.getCity());
			stmt.setString(11, paymentMethod.getState());
			stmt.setString(12, paymentMethod.getZip());
			stmt.setString(13, paymentMethod.getCountry());
			stmt.setInt(14, paymentMethod.getCreatorId());
			
			if(paymentMethod.isCurrent())
				stmt.setInt(15, 1);
			else
				stmt.setInt(15, 0);
			
			if(paymentMethod.isFederalFunding())
				stmt.setInt(16, 1);
			else
				stmt.setInt(16, 0);

            stmt.setBigDecimal(17, paymentMethod.getPoAmount());
			stmt.setInt(18,paymentMethod.getId());
			
			int numRowsInserted = stmt.executeUpdate();
			if(numRowsInserted == 0) {
				throw new SQLException("Updating payment method failed, no rows affected.");
			}
			
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException ignored){}
			if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
			if(rs != null) try {rs.close();} catch(SQLException ignored){}
		}
	}

    private Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getMainDbConnection();
    }

    public void deletePaymentMethod(int paymentMethodId) throws SQLException {
	
		String sql = "DELETE FROM paymentMethod WHERE id="+paymentMethodId;
		Connection conn = null;
		Statement stmt = null;
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			int numRowsDeleted = stmt.executeUpdate(sql);
			
			if(numRowsDeleted == 0) {
				throw new SQLException("Deleting payment method failed, no rows affected.");
			}
			
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException ignored){}
			if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
		}
	}

    public BigDecimal getCost(int paymentMethodId) throws SQLException
    {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT iup.paymentMethodID, SUM((iup.percentPayment * ir.fee)/ 100.0) AS cost");
        sql.append(" FROM (instrumentUsagePayment iup, instrumentUsage iu, instrumentRate ir)");
        sql.append(" WHERE iup.instrumentUsageID = iu.id");
        sql.append(" AND iu.instrumentRateID = ir.id");
        sql.append(" AND paymentMethodID=").append(paymentMethodId);
        sql.append(" GROUP BY paymentMethodID");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql.toString());

            if(rs.next()) {

                BigDecimal cost = rs.getBigDecimal("cost");
                return cost.setScale(2, RoundingMode.CEILING);
            }

            return new BigDecimal("0");
        }
        finally {
            if(conn != null) try {conn.close();} catch(SQLException ignored){}
            if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
            if(rs != null) try {rs.close();} catch(SQLException ignored){}
        }
    }

    public BigDecimal getInvoicedCost(int paymentMethodId) throws SQLException
    {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT iup.paymentMethodID, SUM((iup.percentPayment * ir.fee)/ 100.0) AS cost");
        sql.append(" FROM (instrumentUsagePayment iup, instrumentUsage iu, instrumentRate ir, invoiceInstrumentUsage invoice)");
        sql.append(" WHERE iup.instrumentUsageID = iu.id");
        sql.append(" AND iu.instrumentRateID = ir.id");
        sql.append(" AND invoice.instrumentUsageID = iu.id");
        sql.append(" AND paymentMethodID=").append(paymentMethodId);
        sql.append(" GROUP BY paymentMethodID");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql.toString());

            if(rs.next()) {

                BigDecimal cost = rs.getBigDecimal("cost");
                return cost.setScale(2, RoundingMode.CEILING);
            }

            return new BigDecimal("0");
        }
        finally {
            if(conn != null) try {conn.close();} catch(SQLException ignored){}
            if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
            if(rs != null) try {rs.close();} catch(SQLException ignored){}
        }
    }
}
