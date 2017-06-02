package org.uwpr;

import org.apache.log4j.Logger;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.InputStream;
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

    private static String FROM_ADDRESS;

    private static final Logger log = Logger.getLogger(AppProperties.class.getName());

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
            FROM_ADDRESS = props.getProperty("email.from.address", "do_not_reply@localhost.com");

        } catch (IOException e) {
            log.error("Error reading properties file db.properties", e);
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

    public static Address getFromAddress() throws AddressException
    {
        return new InternetAddress(FROM_ADDRESS);
    }
}
