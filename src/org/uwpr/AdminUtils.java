package org.uwpr;

import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.Groups;

import java.util.List;

/**
 * Created by vsharma on 5/10/2017.
 */
public class AdminUtils
{
    public static List<Researcher> getNotifyAdmins()
    {
        return Groups.getInstance().getMembers(Groups.NOTIFY);
    }
}
