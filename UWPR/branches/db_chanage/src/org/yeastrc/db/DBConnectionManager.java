package org.yeastrc.db;

import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnectionManager {

    public static String MSDATA;
    public static String PR;
    public static String MAINDB;

    private static final Logger log = Logger.getLogger(DBConnectionManager.class.getName());

    static
    {
        Properties props = new Properties();
        InputStream inputStr = null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            inputStr = classLoader.getResourceAsStream("db.properties");
            props.load(inputStr);

            MSDATA = props.getProperty("db.msData", "msData");
            PR = props.getProperty("db.pr", "pr");
            MAINDB = props.getProperty("db.mainDb", "mainDb");
        }
        catch (IOException e)
        {
            log.error("Error reading properties file db.properties", e);
        }
        finally {
            if(inputStr != null) try {inputStr.close();} catch(IOException ignored){}
        }

    }

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

    public static Connection getPrConnection() throws SQLException
    {
        return getConnection(PR);
    }

    public static Connection getMainDbConnection() throws SQLException
    {
        return getConnection(MAINDB);
    }
}