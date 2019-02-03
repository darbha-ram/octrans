package app.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


/**
 * Data model class, encapsulates methods to make SQL queries on OC Transpo Bus
 * cancellations DB in Azure.  The SQL below assumes a MS SQL Server implementation,
 * e.g. the DATEPART() function to query components of MS SQL SMALLDATETIME type,
 * TOP <n> to limit to specified # rows.
 * 
 * @author ramdarbha
 */

@Component
public class OcTranspoModel {

    private static final Logger log = LoggerFactory.getLogger(OcTranspoModel.class);

    @Autowired
    private JdbcTemplate jt_m;

    //
    // Query strings
    //
    
    // SELECT statements
    private static final String getBus_s = "SELECT * FROM buscancellations WHERE busnumber = ? ";
    private static final String select_s = "SELECT ";
    private static final String top_n_s  = " TOP (?) ";
    private static final String getCounts_s = " busnumber, count(1) as num from buscancellations WHERE busnumber > 0 ";
    private static final String getDelays_s = " busnumber, sum(nextminutes) as num from buscancellations WHERE busnumber > 0 ";

    // am-pm WHERE clauses
    private static final String hourClause_s = " DATEPART(HH, cancelledstarttime) ";
    private static final String am_s = hourClause_s + " < ? ";
    private static final String pm_s = hourClause_s + " >= ? ";

    // start & end date WHERE clauses
    private static final String startdateClause_s = " cancelledstarttime > CAST(? AS SMALLDATETIME) ";
    private static final String enddateClause_s   = " cancelledstarttime < CAST(? AS SMALLDATETIME) ";

    // aggregate by busnumber GROUP & ORDER suffixes
    private static final String groupOrderSuffix_s = " GROUP BY busnumber ORDER BY num ";
    private static final String least_s = " ASC";  // ASC is default
    private static final String most_s = " DESC";
    
    private static final String and_s = " AND ";

    
    /**
     * getBus - given a bus#, am/pm/all flag, optional start and end dates, find all
     *     cancellations for the bus in the time of day, between the start & end dates. 
     * @param busnum e.g. 256
     * @param ampm  "am", "pm" or "all"
     * @param startdate as yyyy-mm-dd e.g. 2018-11-31
     * @param enddate as yyyy-mm-dd  e.g. 2018-12-21
     * @return collection of Cancellation records
     */
    public List<Cancellation> getBus(final int busnum, final String ampm,
    	final String startdate, final String enddate)
    {
        log.info("getBus: entry for bus " + busnum + ", " + ampm + ", " + startdate + " ~ " + enddate);
        
        List<Object> queryArgsList = new ArrayList<Object>();

        String queryString = getBus_s;
        queryArgsList.add(busnum);
        
        queryString = processTimeInputs(queryString, queryArgsList, ampm, startdate, enddate);

        // convert list to array, so it can be used in query
        Object[] queryArgs = new Object[queryArgsList.size()];
        queryArgs = queryArgsList.toArray(queryArgs);
        
        // return set
    	List<Cancellation> cancels = new ArrayList<Cancellation>();
    	
        jt_m.query(
            queryString, queryArgs, (rs, rowNum) ->
            new Cancellation(
                rs.getInt("busnumber"),
                rs.getString("busname"),
                rs.getString("cancelledstarttime"),
                rs.getString("cancelledstartloc"),
                rs.getInt("nextminutes"))
        ).forEach(can -> cancels.add(can));
        
        log.info("getBus: got " + cancels.size() + " rows.");
        return cancels;
    }
	
	
    /**
     * getCounts - given a number e.g. 15, am/pm/all flag, optional start and end dates, find the
     *     top (or bottom) 15 buses with the most (or least) cancellations during that time of day,
     *     between start & end dates. 
     * @param num - number of entries to return
     * @param ampm - "am", "pm" or "all"
     * @param startdate in yyyy-mm-dd e.g. 2018-01-04
     * @param enddate in yyyy-mm-dd e.g. 2018-02-25
     * @param least true if querying for least, false if querying for most
     * @return - list of records, each record holding bus no. and its no. cancellations in interval.
     */
    public List<BusRecord> getCounts(final int num, final String ampm,
    	final String startdate, final String enddate, final boolean least)
    {
        log.info("getCounts: top " + num + ", " + ampm + ", " + startdate + " ~ " + enddate + (least?" :L":" :M"));
        
        List<Object> queryArgsList = new ArrayList<Object>();

        String queryString = null;
        if (num > 0)
        {
            queryString = select_s + top_n_s + getCounts_s;
            queryArgsList.add(num);
        }
        else
            queryString = select_s + getCounts_s;
        
        queryString = processTimeInputs(queryString, queryArgsList, ampm, startdate, enddate);
        
        // finally, append GROUP & ORDER suffixes
        if (least)
        	queryString += groupOrderSuffix_s + least_s;
        else
        	queryString += groupOrderSuffix_s + most_s;

        // convert list to array, so it can be used in query
        Object[] queryArgs = new Object[queryArgsList.size()];
        queryArgs = queryArgsList.toArray(queryArgs);
        
        // return set
    	List<BusRecord> tuples = new ArrayList<BusRecord>();
    	
        jt_m.query(
            queryString, queryArgs, (rs, rowNum) ->
            new BusRecord(
                rs.getInt("busnumber"),
                rs.getInt("num"))
        ).forEach(can -> tuples.add(can));
        
        log.info("getCounts: got " + tuples.size() + " rows.");
        return tuples;
    }

    
    /**
     * getDelays - given a number e.g. 15, am/pm/all flag, optional start and end dates, find the
     *     top (or bottom) 15 buses with the most (or least) total delay during that time of day,
     *     between start & end dates. 
     * @param num - number of entries to return
     * @param ampm - "am", "pm" or "all"
     * @param startdate in yyyy-mm-dd e.g. 2018-01-04
     * @param enddate in yyyy-mm-dd e.g. 2018-02-25
     * @param least true if querying for least, false if querying for most
     * @return - list of records, each record holding bus no. and its total delay in minutes.
     */
    public List<BusRecord> getDelays(final int num, final String ampm,
    	final String startdate, final String enddate, final boolean least)
    {
        log.info("getDelays: top " + num + ", " + ampm + ", " + startdate + " ~ " + enddate + (least?" :L":" :M"));
        
        List<Object> queryArgsList = new ArrayList<Object>();

        String queryString = null;
        if (num > 0)
        {
            queryString = select_s + top_n_s + getDelays_s;
            queryArgsList.add(num);
        }
        else
            queryString = select_s + getDelays_s;
        
        queryString = processTimeInputs(queryString, queryArgsList, ampm, startdate, enddate);
        
        // finally, append GROUP & ORDER suffixes
        if (least)
        	queryString += groupOrderSuffix_s + least_s;
        else
        	queryString += groupOrderSuffix_s + most_s;

        // convert list to array, so it can be used in query
        Object[] queryArgs = new Object[queryArgsList.size()];
        queryArgs = queryArgsList.toArray(queryArgs);
        
        // return set
    	List<BusRecord> tuples = new ArrayList<BusRecord>();
    	
        jt_m.query(
            queryString, queryArgs, (rs, rowNum) ->
            new BusRecord(
                rs.getInt("busnumber"),
                rs.getInt("num"))
        ).forEach(can -> tuples.add(can));
        
        log.info("getDelays: got " + tuples.size() + " rows.");
        return tuples;
    }

    
    //
    // Helper methods
    //
    
    /*
     * Helper method to append to query string and arguments list, based on ampm flag & start/end dates.
     * Returns updated query string, after adding to argument list if needed.
     */
    private String processTimeInputs(final String query, final List<Object> argsList,
    	final String ampm, final String sd, final String ed)
    {
    	String queryString = query;
    	
        if (ampm.equalsIgnoreCase("am")) {
        	queryString += and_s + am_s;
        	argsList.add(12);
        }
        else if (ampm.equalsIgnoreCase("pm")) {
        	queryString += and_s + pm_s;
        	argsList.add(12);
        }
 
	    if (sd != null) {
	    	queryString += and_s + startdateClause_s;
	    	argsList.add(sd + " 00:00:00 ");
	    }
	    if (ed != null) {
	    	queryString += and_s + enddateClause_s;
	    	argsList.add(ed + " 23:59:59 ");
	    }
	    
	    return queryString;
    }
    
    
}
