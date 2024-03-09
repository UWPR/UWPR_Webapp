package org.uwpr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by vsharma on 2/13/15.
 */
public class AppProperties
{
    private static String DB_MSDATA;
    private static String DB_PR;
    private static String DB_MAINDB;
    private static String WEB_MSDAPL_BASEURI;

    private static String HOST;
    private static String LOGIN_PAGE;

    private static MailProperties _mailProps;

    private static final Logger log = LogManager.getLogger(AppProperties.class.getName());

    static {
        Properties props = new Properties();
        InputStream inputStr = null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            inputStr = classLoader.getResourceAsStream("application.properties");
            props.load(inputStr);

            DB_MSDATA = props.getProperty("db.msData", "msData");
            DB_PR = props.getProperty("db.pr", "pr");
            DB_MAINDB = props.getProperty("db.mainDb", "mainDb");
            WEB_MSDAPL_BASEURI = props.getProperty("web.msdapl.baseuri", "http://localhost:8080/msdapl");
            HOST = props.getProperty("web.host", "http://localhost:8080");
            LOGIN_PAGE = props.getProperty("web.login", HOST + "/pr/pages/login/login_retry.jsp");

        } catch (IOException e) {
            log.error("Error reading properties file application.properties", e);
        } finally {
            if (inputStr != null) try {
                inputStr.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static String getDbMsData()
    {
        return DB_MSDATA;
    }

    public static String getDbMainDb()
    {
        return DB_MAINDB;
    }

    public static String getDbPr()
    {
        return DB_PR;
    }

    public static String getWebMsdaplBaseuri()
    {
        return WEB_MSDAPL_BASEURI;
    }

    public static String getHost()
    {
        return HOST;
    }

    public static String getLoginPage()
    {
        return LOGIN_PAGE;
    }

    public static MailProperties getMailProps()
    {
        if (_mailProps == null)
        {
            queryAppConfigs();
        }
        return _mailProps;
    }

    private static void queryAppConfigs()
    {
        Map<String, String>propMap = new HashMap<>();

        Statement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM config_msdapl_webapp";
        Connection conn = null;
        try
        {
            conn = DBConnectionManager.getMainDbConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql.toString());

            while(rs.next())
            {
                String propName = rs.getString("config_key");
                String propValue = rs.getString("config_value");
                propMap.put(propName, propValue);
            }
        } catch (SQLException e)
        {
            log.error("Error reading properties from the database table mainDb.config_msdapl_webapp", e);
        }
        finally
        {
            if(stmt != null) try {stmt.close();} catch(SQLException e){}
            if(rs != null) try {rs.close();} catch(SQLException e){}
            if(conn != null) try {conn.close();} catch(SQLException e){}
        }

        _mailProps = new MailProperties(propMap);
    }

    public static class MailProperties
    {
        private String _smtpHost;
        private String _smtpPort;
        private String _senderEmail;
        private String _senderPassword;

        MailProperties(Map<String, String> properties)
        {
            for (Map.Entry<String, String> entry : properties.entrySet())
            {
                if ("mail.smtp.host".equals(entry.getKey()))
                {
                    _smtpHost = entry.getValue();
                }
                else if ("mail.smtp.port".equals(entry.getKey()))
                {
                    _smtpPort = entry.getValue();
                }
                else if ("from.email.address".equals(entry.getKey()))
                {
                    _senderEmail = entry.getValue();
                }
                else if ("from.email.password".equals(entry.getKey()))
                {
                    _senderPassword = entry.getValue();
                }
            }
        }
        public String getSmtpHost()
        {
            return _smtpHost;
        }

        public String getSmtpPort()
        {
            return _smtpPort;
        }

        public String getSenderEmail()
        {
            return _senderEmail;
        }

        public String getSenderPassword()
        {
            return _senderPassword;
        }
    }
}
