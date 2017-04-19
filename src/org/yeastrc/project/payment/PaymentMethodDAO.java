/**
 * PaymentMethodDAO.java
 * @author Vagisha Sharma
 * May 20, 2011
 */
package org.yeastrc.project.payment;

import org.apache.log4j.Logger;
import org.uwpr.costcenter.InstrumentRate;
import org.uwpr.instrumentlog.UsageBlockBaseFilter;
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
		paymentMethod.setBudgetExpirationDate(rs.getDate("budgetExpirationDate"));
        paymentMethod.setPonumber(rs.getString("PONumber"));
		paymentMethod.setPaymentMethodName(rs.getString("paymentMethodName"));
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

        paymentMethod.setInstrumentCost(getInstrumentCost(paymentMethod.getId()));
		paymentMethod.setSignupFee(getSignupFee(paymentMethod.getId()));
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

	
	public int savePaymentMethod(Connection conn, PaymentMethod paymentMethod) throws SQLException {
		
		String sql = "INSERT INTO paymentMethod (UWBudgetNumber, PONumber, budgetExpirationDate, paymentMethodName, contactNameFirst, contactLastName, contactEmail,";
		sql += " contactPhone, organization, addressLine1, addressLine2, city, state, zip, country, ";
		sql += " dateCreated,  createdBy, isCurrent, federalFunding, POAmount) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			int i = 1;
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(i++, paymentMethod.getUwbudgetNumber());
			stmt.setString(i++, paymentMethod.getPonumber());
			java.sql.Date expDate = paymentMethod.getBudgetExpirationDate() == null ? null : new java.sql.Date(paymentMethod.getBudgetExpirationDate().getTime());
			stmt.setDate(i++, expDate);
			stmt.setString(i++, paymentMethod.getPaymentMethodName());
			stmt.setString(i++, paymentMethod.getContactFirstName());
			stmt.setString(i++, paymentMethod.getContactLastName());
			stmt.setString(i++, paymentMethod.getContactEmail());
			stmt.setString(i++, paymentMethod.getContactPhone());
			stmt.setString(i++, paymentMethod.getOrganization());
			stmt.setString(i++, paymentMethod.getAddressLine1());
			stmt.setString(i++, paymentMethod.getAddressLine2());
			stmt.setString(i++, paymentMethod.getCity());
			stmt.setString(i++, paymentMethod.getState());
			stmt.setString(i++, paymentMethod.getZip());
			stmt.setString(i++, paymentMethod.getCountry());
			stmt.setTimestamp(i++, new Timestamp(new Date().getTime()));
			stmt.setInt(i++, paymentMethod.getCreatorId());
			stmt.setInt(i++, 1);
			if(paymentMethod.isFederalFunding()) {
				stmt.setInt(i, 1);
			}
			else {
				stmt.setInt(i, 0);
			}
			stmt.setBigDecimal(++i, paymentMethod.getPoAmount());

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
			if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
			if(rs != null) try {rs.close();} catch(SQLException ignored){}
		}
	}
	
	public void updatePaymentMethod(PaymentMethod paymentMethod) throws SQLException {
		
		String sql = "UPDATE paymentMethod ";
		sql += "SET UWBudgetNumber = ?";
		sql += ", PONumber = ?";
		sql += ", budgetExpirationDate = ?";
		sql += ", paymentMethodName = ?";
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
			int i = 0;
			stmt.setString(++i, paymentMethod.getUwbudgetNumber());
			stmt.setString(++i, paymentMethod.getPonumber());
			java.sql.Date expirationDate = paymentMethod.getBudgetExpirationDate() == null ? null : new java.sql.Date(paymentMethod.getBudgetExpirationDate().getTime());
			stmt.setDate(++i, expirationDate);
			stmt.setString(++i, paymentMethod.getPaymentMethodName());
			stmt.setString(++i, paymentMethod.getContactFirstName());
			stmt.setString(++i, paymentMethod.getContactLastName());
			stmt.setString(++i, paymentMethod.getContactEmail());
			stmt.setString(++i, paymentMethod.getContactPhone());
			stmt.setString(++i, paymentMethod.getOrganization());
			stmt.setString(++i, paymentMethod.getAddressLine1());
			stmt.setString(++i, paymentMethod.getAddressLine2());
			stmt.setString(++i, paymentMethod.getCity());
			stmt.setString(++i, paymentMethod.getState());
			stmt.setString(++i, paymentMethod.getZip());
			stmt.setString(++i, paymentMethod.getCountry());
			stmt.setInt(++i, paymentMethod.getCreatorId());

			i++;
			if(paymentMethod.isCurrent())
				stmt.setInt(i, 1);
			else
				stmt.setInt(i, 0);
			i++;
			if(paymentMethod.isFederalFunding())
				stmt.setInt(i, 1);
			else
				stmt.setInt(i, 0);

            stmt.setBigDecimal(++i, paymentMethod.getPoAmount());
			stmt.setInt(++i,paymentMethod.getId());
			
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

    private BigDecimal getInstrumentCost(int paymentMethodId) throws SQLException
    {
        BigDecimal cost = getCost(paymentMethodId, UsageBlockBaseFilter.BlockType.INSTRUMENT_USAGE);
		cost = cost.multiply(InstrumentRate.INSTRUMENT_PERC);
		return cost.setScale(2, RoundingMode.HALF_UP);
    }

	private BigDecimal getSignupFee(int paymentMethodId) throws SQLException
	{
		BigDecimal cost = getCost(paymentMethodId, UsageBlockBaseFilter.BlockType.ALL);
		cost = cost.multiply(InstrumentRate.SIGNUP_PERC);
		return cost.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal getCost(int paymentMethodId, UsageBlockBaseFilter.BlockType blockType) throws SQLException
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT iup.paymentMethodID, SUM((iup.percentPayment * ir.fee)/ 100.0) AS cost");
		sql.append(" FROM (instrumentUsagePayment iup, instrumentUsage iu, instrumentRate ir)");
		sql.append(" WHERE iup.instrumentUsageID = iu.id ");
		if(blockType == UsageBlockBaseFilter.BlockType.INSTRUMENT_USAGE)
		{
			sql.append(" AND iu.deleted=0"); // Get ONLY instrument usage
		}
		else if(blockType == UsageBlockBaseFilter.BlockType.SIGNUP_ONLY)
		{
			sql.append(" AND iu.deleted=1"); // Get ONLY signup blocks
		}

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
				return cost;
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
		BigDecimal instrumentCost = getInvoicedInstrumentCost(paymentMethodId);
		BigDecimal signupFee = getInvoicedSignupFee(paymentMethodId);
		return instrumentCost.add(signupFee);
    }

	private BigDecimal getInvoicedInstrumentCost(int paymentMethodId) throws SQLException
	{
		BigDecimal cost = getInvoicedCost(paymentMethodId, UsageBlockBaseFilter.BlockType.INSTRUMENT_USAGE);
		cost = cost.multiply(InstrumentRate.INSTRUMENT_PERC);
		return cost.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal getInvoicedSignupFee(int paymentMethodId) throws SQLException
	{
		BigDecimal cost = getInvoicedCost(paymentMethodId, UsageBlockBaseFilter.BlockType.ALL);
		cost = cost.multiply(InstrumentRate.SIGNUP_PERC);
		return cost.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal getInvoicedCost(int paymentMethodId, UsageBlockBaseFilter.BlockType blockType) throws SQLException
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT iup.paymentMethodID, SUM((iup.percentPayment * ir.fee)/ 100.0) AS cost");
		sql.append(" FROM (instrumentUsagePayment iup, instrumentUsage iu, instrumentRate ir, invoiceInstrumentUsage invoice)");
		sql.append(" WHERE iup.instrumentUsageID = iu.id");
		if(blockType == UsageBlockBaseFilter.BlockType.INSTRUMENT_USAGE)
		{
			sql.append(" AND iu.deleted=0"); // Get ONLY instrument usage
		}
		else if(blockType == UsageBlockBaseFilter.BlockType.SIGNUP_ONLY)
		{
			sql.append(" AND iu.deleted=1"); // Get ONLY signup blocks
		}
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
				return cost;
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
