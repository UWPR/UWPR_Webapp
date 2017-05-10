package org.uwpr;

import org.apache.log4j.Logger;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.Groups;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vsharma on 5/10/2017.
 */
public class AdminUtils
{
    private static final Logger log = Logger.getLogger(AdminUtils.class);

    public static List<Researcher> getNotifyAdmins()
    {
        return Groups.getInstance().getMembers(Groups.NOTIFY);
    }

    public static String getHost()
    {
        return "http://proteomicsresource.washington.edu";
    }

    public static Address getFromAddress() throws AddressException
    {
        return new InternetAddress("do_not_reply@proteomicsresource.washington.edu");
    }
}
