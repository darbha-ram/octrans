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

    
    public List<Cancellation> getBus(final int busnum, final String ampm,
    	final LocalDateTime sd, final LocalDateTime ed)
    {
        log.info("getBus: entry for bus " + busnum);

    	// TODO - implement ampm, sd, ed
    	List<Cancellation> cancels = new ArrayList<Cancellation>();
    	
        jt_m.query(
            "SELECT * FROM buscancellations WHERE busnumber = ?", new Object[] { busnum }, (rs, rowNum) ->
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
