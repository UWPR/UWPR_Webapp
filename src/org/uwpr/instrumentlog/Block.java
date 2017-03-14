package org.uwpr.instrumentlog;

import java.util.Date;

/**
 * Created by vsharma on 3/8/2017.
 */
public interface Block
{
    public int getProjectID();

    public int getInstrumentID();

    public Date getStartDate();

    public Date getEndDate();

    public Date getDateCreated();
}
