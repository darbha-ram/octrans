package app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


/**
 * Data model class, encapsulates methods to make SQL queries on OC Transpo Bus
 * cancellations DB in Azure.
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
    private static final String am_s = hourClause_s + " < 12 ";
    private static final String pm_s = hourClause_s + " >= 12 ";
    		
    private static final String getBus_s = "SELECT * FROM buscancellations WHERE busnumber = ? ";

    
    public List<Cancellation> getBus(final int busnum, final String ampm,
    	final LocalDateTime sd, final LocalDateTime ed)
    {
        log.info("getBus: entry for bus " + busnum);

        String queryString = getBus_s;
        if (ampm.equalsIgnoreCase("am"))
        	queryString += am_s;
        else if (ampm.equalsIgnoreCase("pm"))
        	queryString += pm_s;
        
    	// TODO - implement sd, ed
    	List<Cancellation> cancels = new ArrayList<Cancellation>();
    	
        jt_m.query(
            queryString, new Object[] { busnum }, (rs, rowNum) ->
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
