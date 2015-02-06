package org.uwpr.costcenter;

import org.apache.commons.dbcp.BasicDataSource;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.MsInstrumentUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: vsharma
 * Date: 2/14/14
 * Time: 11:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class ParseAndUploadRates
{

    public static void main(String[] args) throws SQLException, IOException
    {
        boolean test = true;
        if(args.length > 1)
        {
            test = Boolean.parseBoolean(args[1]);
        }
        if(test)
        {
            System.out.println("Running in test mode");
        }

        ParseAndUploadRates parser = new ParseAndUploadRates();
        Map<String, MsInstrument> instrumentNameMap = parser.getInstruments();
        Map<String, RateType> rateTypeNameMap = parser.getRateTypes();
        Map<String, TimeBlock> timeBlockMap = parser.getTimeBlocks();

        List<InstrumentRate> newRates = parser.readRates(args[0], instrumentNameMap, rateTypeNameMap, timeBlockMap);
        System.out.println("Read " + newRates.size() + " new rates");


        Connection conn = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        int countUpdate = 0;
        int countInsert = 0;
        try
        {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Set the current rates to false
            String updateSQL = "UPDATE instrumentRate set isCurrent = 0 WHERE instrumentID = ? AND rateTypeID = ? AND isCurrent=1";
            updateStmt = conn.prepareStatement(updateSQL);

            for(MsInstrument instrument: instrumentNameMap.values())
            {
                for(RateType rateType: rateTypeNameMap.values())
                {
                    updateStmt.setInt(1, instrument.getID());
                    updateStmt.setInt(2, rateType.getId());
                    if(!test)
                        countUpdate += updateStmt.executeUpdate();
                }
            }

            insertStmt = conn.prepareStatement(InstrumentRateDAO.saveInstrumentRateSql);
            for(InstrumentRate newRate: newRates)
            {
                if(!test)
                    countInsert += InstrumentRateDAO.getInstance().saveInstrumentRate(newRate, insertStmt);
            }
            conn.commit();
        }
        finally
        {
            if(conn != null) try {conn.close();} catch(SQLException e){}
            if(insertStmt != null) try {insertStmt.close();} catch(SQLException e){}
            if(updateStmt != null) try {updateStmt.close();} catch(SQLException e){}
        }
        System.out.println("Updated " + countUpdate + " old rates");
        System.out.println("Saved " + countInsert + " new rates");
    }

    public List<InstrumentRate> readRates(String file, Map<String, MsInstrument> instruments, Map<String, RateType> rateTypes, Map<String, TimeBlock> timeBlocks) throws IOException {

        List<InstrumentRate> newRates = new ArrayList<InstrumentRate>();
        BufferedReader reader = null;
        String sep = "\\t";
        try
        {
            reader = new BufferedReader(new FileReader(file));

            RateType currentRateType = null;
            String line = null;

            MsInstrument[] instrumentCol = null;

            while((line = reader.readLine()) != null)
            {
                if(line.trim().length() == 0)
                    continue;

                if(line.toLowerCase().contains("rates"))
                {
                    currentRateType = rateTypes.get(line.trim().split(sep)[0]);
                    if(currentRateType == null)
                    {
                        throw new IllegalStateException("Could not find rateType for " + line.trim());
                    }

                    // Read instruments
                    line = reader.readLine();
                    String[] tokens = line.split(sep);
                    instrumentCol = new MsInstrument[tokens.length];
                    for(int i = 1; i < tokens.length; i++)
                    {
                        String token = tokens[i].trim();
                        MsInstrument instrument = instruments.get(token.trim());
                        if(instrument == null)
                        {
                            throw new IllegalStateException("Could not find instrument for " + token);
                        }
                        instrumentCol[i] = instrument;
                    }

                    // Skip next line
                    reader.readLine();

                    continue;
                }


                String[] tokens = line.trim().split(sep);
                TimeBlock currentTimeBlock = null;
                for(int i = 0; i < tokens.length; i++)
                {
                    String token = tokens[i].trim();
                    if(i == 0)
                    {
                        currentTimeBlock = timeBlocks.get(token);
                        if(currentTimeBlock == null)
                        {
                            throw new IllegalStateException("Could not find timeBlockId for " + token);
                        }
                        continue;
                    }

                    if(currentRateType == null)
                    {
                        throw new IllegalStateException("No current rate type");
                    }
                    if(currentTimeBlock == null)
                    {
                        throw new IllegalStateException("No current time block");
                    }
                    if(instrumentCol[i] == null)
                    {
                        throw new IllegalStateException("No instrument");
                    }

                    InstrumentRate newRate = new InstrumentRate();
                    newRate.setCurrent(true);
                    newRate.setInstrument(instrumentCol[i]);
                    newRate.setRateType(currentRateType);
                    newRate.setTimeBlock(currentTimeBlock);
                    String rate = token.replaceAll("\\$", "").replaceAll("\"", "").trim().replaceAll(",", "");
                    try {
                        newRate.setRate(new BigDecimal(rate));
                    }
                    catch(NumberFormatException e)
                    {
                        System.out.println("Unparsable rate " + rate + " in line " + line + " and token " + token);
                    }

                    newRates.add(newRate);
                }
            }

        }
        finally
        {
            if(reader != null) try {reader.close();} catch(IOException ignored){}
        }

        return newRates;
    }

    public Map<String, MsInstrument> getInstruments() throws SQLException {

        Map<String, MsInstrument> instrumentMap = new HashMap<String, MsInstrument>();

        Connection connection = null;
        try
        {
            connection = getConnection();
            List<MsInstrument> instruments = MsInstrumentUtils.instance().getMsInstruments(connection, true); // active instruments

            for(MsInstrument instrument: instruments)
            {
                if(instrument.getName().equals("LTQ-Orbitrap-1"))
                {
                    instrumentMap.put("OT1", instrument);
                }
                else if(instrument.getName().equals("LTQ-Orbitrap-2"))
                {
                    instrumentMap.put("OT2", instrument);
                }
                else if(instrument.getName().equals("TSQ-Access"))
                {
                    instrumentMap.put("TSQA", instrument);
                }
                else if(instrument.getName().equals("TSQ-Vantage"))
                {
                    instrumentMap.put("TSQV", instrument);
                }
                else if(instrument.getName().equals("Q"))
                {
                    instrumentMap.put("QE +", instrument);
                }
                else if(instrument.getName().equals("Fusion"))
                {
                    instrumentMap.put("Fusion", instrument);
                }
            }
        }
        finally
        {
            if(connection != null) try {connection.close();}catch(SQLException ignored){}
        }
        return instrumentMap;
    }

    public Map<String, RateType> getRateTypes() throws SQLException
    {
        Map<String, RateType> rateTypeMap = new HashMap<String, RateType>();
        Connection connection = null;
        try
        {
            connection = getConnection();
            List<RateType> rateTypes = RateTypeDAO.getInstance().getAllRateTypes(connection);
            for(RateType rateType: rateTypes)
            {
                if(rateType.getName().equals("UW"))
                {
                    rateTypeMap.put("UW Internal Rates Without Labor", rateType);
                }
                else if(rateType.getName().equals("UW_FFS"))
                {
                    rateTypeMap.put("UW Internal Rates fee-for-service With Labor", rateType);
                }
                else if(rateType.getName().equals("NON_PROFIT_FFS"))
                {
                    rateTypeMap.put("External Billing Rates - Non Profit With Labor", rateType);
                }
                else if(rateType.getName().equals("COMMERCIAL_FFS"))
                {
                   rateTypeMap.put("External Billing Rates - Commercial With Labor", rateType);
                }
            }
        }
        finally
        {
            if(connection != null) try {connection.close();}catch(SQLException ignored){}
        }

        return rateTypeMap;
    }

    public Map<String, TimeBlock> getTimeBlocks() throws SQLException
    {
        Map<String, TimeBlock> timeBlockMap = new HashMap<String, TimeBlock>();

        Connection connection = null;
        try
        {
            connection = getConnection();

            List<TimeBlock> timeBlocks = TimeBlockDAO.getInstance().getAllTimeBlocks(connection);
            for(TimeBlock timeBlock: timeBlocks)
            {
                timeBlockMap.put(String.valueOf(timeBlock.getNumHours()), timeBlock);
            }
        }
        finally
        {
            if(connection != null) try {connection.close();}catch(SQLException ignored){}
        }

        return timeBlockMap;
    }

    private static BasicDataSource dataSource = null;
    static
    {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost/pr");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setMaxActive(10);
    }

    private static Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

}
