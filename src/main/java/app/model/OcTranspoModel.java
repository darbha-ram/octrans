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
 * e.g. the DATEPART() function to query components of MS SQL SMALLDATETIME type.
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
    
    private static final String hourClause_s = " AND DATEPART(HH, cancelledstarttime) ";
    private static final String am_s = hourClause_s + " < ? ";
    private static final String pm_s = hourClause_s + " >= ? ";
    		
    private static final String getBus_s = "SELECT * FROM buscancellations WHERE busnumber = ? ";

    
    /**
     * getBus - given a bus#, am/pm/all flag, optional start and end dates, find all
     *     cancellations for the bus in the time of day, between the start & end dates. 
     * @param busnum e.g. 256
     * @param ampm  "am", "pm" or "all"
     * @param startdate e.g. 2018-11-31
     * @param enddate   e.g. 2018-12-21
     * @return collection of Cancellation records
     */
    public List<Cancellation> getBus(final int busnum, final String ampm,
    	final String startdate, final String enddate)
    {
        log.info("getBus: entry for bus " + busnum + ", " + ampm + ", " + startdate + " ~ " + enddate);
        
        List<Object> queryArgsList = new ArrayList<Object>();

        String queryString = getBus_s;
        queryArgsList.add(busnum);
        
        if (ampm.equalsIgnoreCase("am"))
        	{ queryString += am_s; queryArgsList.add(12); }
        else if (ampm.equalsIgnoreCase("pm"))
        	{ queryString += pm_s; queryArgsList.add(12); }
     
        if (startdate != null) {
        	queryString += " AND cancelledstarttime > CAST(? AS SMALLDATETIME) " ;
        	queryArgsList.add(startdate + " 00:00:00 ");
        }
        if (enddate != null) {
        	queryString += " AND cancelledstarttime < CAST(? AS SMALLDATETIME) " ;
        	queryArgsList.add(enddate + " 23:59:59 ");
        }

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
	
	
}
