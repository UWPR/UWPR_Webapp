package org.yeastrc.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uwpr.AppProperties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnectionManager {

    private static final Logger log = LogManager.getLogger(DBConnectionManager.class);

    public static String MSDATA = AppProperties.getDbMsData();
    public static String PR = AppProperties.getDbPr();
    public static String MAINDB = AppProperties.getDbMainDb();

    public static Connection getConnection(String db) throws SQLException {
		try {
			Context ctx = new InitialContext();	
			DataSource ds;
			Connection conn;

			if (db.equals(PR)) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/" + PR); }
            else if(db.equals(MSDATA)) {ds = (DataSource)ctx.lookup("java:comp/env/jdbc/" + MSDATA);}
            else if(db.equals(MAINDB)) {ds = (DataSource)ctx.lookup("java:comp/env/jdbc/" + MAINDB);}
			else if (db.equals("sgd")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/sgd"); }
			else if (db.equals("sgd_static_200409")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/sgd_static_200409"); }
			else if (db.equals("go")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/go"); }
			else if (db.equals("go_xref")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/go_xref"); }
			else if (db.equals("wormbase")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/wormbase"); }
			else if (db.equals("nrseq")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/nrseq"); }
			else if (db.equals("scop")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/scop"); }
			else if (db.equals("pdr")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/pdr"); }
			else if (db.equals("hgnc")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/hgnc"); }
			
			else { throw new SQLException("Invalid database name passed into DBConnectionManager."); }

			if (ds != null) {
				conn = ds.getConnection();
				if (conn != null) { return conn; }
				else { throw new SQLException("Got a null connection..."); }
			}

			throw new SQLException("Got a null DataSource...");
		} catch (NamingException ne) {
			throw new SQLException("Naming exception: " + ne.getMessage() + "(" + db + ")" );
		}
	}

    /**
     * Ends a transaction and returns the connection to the pool.  Rolls the transaction back unless
     * committed is true, restores autocommit, then closes the connection.  Does nothing for a null
     * connection, so it can be called from a finally block that may not have got one.
     *
     * The rollback comes first.  setAutoCommit(true) on a connection with an open transaction commits
     * that transaction, so restoring autocommit over an abandoned one writes the part of it that ran.
     * Checked against MySQL Connector/J 5.1.32 and MariaDB 10.6 on 2026-09-20.
     *
     * A rollback that throws leaves the transaction open, so autocommit is left alone and the connection
     * is closed as it is.  DBCP rolls back again in passivateObject, and destroys the connection when
     * that rollback throws too, which ends the transaction with the physical connection.
     *
     * Call this from the finally block of every site that calls setAutoCommit(false), with a committed
     * flag set on the line after commit().  A flag rather than a catch block, because an early return or
     * a RuntimeException leaves the try without reaching either the commit or a catch.
     */
    public static void endTransactionAndClose(Connection conn, boolean committed)
    {
        if(conn == null)
        {
            return;
        }

        boolean transactionEnded = committed;
        if(!committed)
        {
            try
            {
                conn.rollback();
                transactionEnded = true;
            }
            catch(SQLException e)
            {
                log.error("Rollback failed. Closing the connection without restoring autocommit.", e);
            }
        }

        if(transactionEnded)
        {
            try { conn.setAutoCommit(true); } catch(SQLException e) { log.error("Could not restore autocommit.", e); }
        }
        try { conn.close(); } catch(SQLException e) { log.error("Could not close the connection.", e); }
    }

    public static Connection getPrConnection() throws SQLException
    {
        return getConnection(PR);
    }

    public static Connection getMainDbConnection() throws SQLException
    {
        return getConnection(MAINDB);
    }

    public static String getInstrumentsTableSQL()
    {
        return DBConnectionManager.MSDATA + ".msInstrument";
    }
}