package app.controller;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.model.Cancellation;
import app.model.BusRecord;
import app.model.OcTranspoModel;


/*
 * REST API
 * 
 * base: /ocapi/v1/
 *     bus/<number>               -- all cancellations of bus
 *     bus/<number>/am or /pm     -- cancellations of bus in morning or evening
 *     bus/<number>?sd=xxx&ed=yyy -- cancellations of bus between start & end dates
 *     
 *     count/
 *         most/<n>                   -- 'n' buses with the most cancellations
 *         least/<n>                  -- 'n' buses with the least cancellations
 *             .. ?sd=xxx&ed=yyy      -- 'n' most or least cancelled buses in given time frame
 *             
 *     delay/
 *         most, least as above
 */

@RestController
public class ApiV1Controller {

    private static final String base_s = "/ocapi/v1/";
    private static final String dateRegex_s = "\\d\\d\\d\\d\\-\\d\\d\\-\\d\\d"; // yyyy-mm-dd
    private static final Pattern pat_s = Pattern.compile(dateRegex_s);

    
    @Autowired
    private OcTranspoModel model_m;
    
    @RequestMapping(base_s)
    public String getIndex()
    {
        return "Root";
    }
    
    
    // use non capturing group in ampm regex
    @RequestMapping(base_s + "bus/{busnum:[\\d]+}/{ampm:^(?:am|pm|all)$}")
    public List<Cancellation> getBus(@PathVariable int busnum, @PathVariable String ampm,
        @RequestParam(required=false) String sd,
        @RequestParam(required=false) String ed)
    {
    	// TODO - try to use annotations above @Valid @Pattern to validate sd & ed

    	Matcher m = null;
    	if (sd != null)
    	{
        	m = pat_s.matcher(sd);
        	if (!m.matches()) sd = null;
    	}
    	if (ed != null)
    	{
        	m = pat_s.matcher(ed);
        	if (!m.matches()) ed = null;
    	}
    	
    	return model_m.getBus(busnum, ampm, sd, ed);
    }

    
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    
    @RequestMapping(base_s + "count/most/{num:[\\d]+}/{ampm:^(?:am|pm|all)$}")
    public List<BusRecord> getMostCount(@PathVariable int num, @PathVariable String ampm,
        @RequestParam(required=false) String sd,
        @RequestParam(required=false) String ed)
    {
    	Matcher m = null;
    	if (sd != null)
    	{
        	m = pat_s.matcher(sd);
        	if (!m.matches()) sd = null;
    	}
    	if (ed != null)
    	{
        	m = pat_s.matcher(ed);
        	if (!m.matches()) ed = null;
    	}

        return model_m.getCounts(num, ampm, sd, ed, false);
    }
    
    
    @RequestMapping(base_s + "count/least/{num:[\\d]+}/{ampm:^(?:am|pm|all)$}")
    public List<BusRecord> getLeastCount(@PathVariable int num, @PathVariable String ampm,
        @RequestParam(required=false) String sd,
        @RequestParam(required=false) String ed)
    {
    	Matcher m = null;
    	if (sd != null)
    	{
        	m = pat_s.matcher(sd);
        	if (!m.matches()) sd = null;
    	}
    	if (ed != null)
    	{
        	m = pat_s.matcher(ed);
        	if (!m.matches()) ed = null;
    	}

        return model_m.getCounts(num, ampm, sd, ed, true);
    }
    
    
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    
    @RequestMapping(base_s + "delay/most/{num:[\\d]+}/{ampm:^(?:am|pm|all)$}")
    public List<BusRecord> getMostDelay(@PathVariable int num, @PathVariable String ampm,
        @RequestParam(required=false) String sd,
        @RequestParam(required=false) String ed)
    {
    	Matcher m = null;
    	if (sd != null)
    	{
        	m = pat_s.matcher(sd);
        	if (!m.matches()) sd = null;
    	}
    	if (ed != null)
    	{
        	m = pat_s.matcher(ed);
        	if (!m.matches()) ed = null;
    	}

        return model_m.getDelays(num, ampm, sd, ed, false);
    }
    
    
    @RequestMapping(base_s + "delay/least/{num:[\\d]+}/{ampm:^(?:am|pm|all)$}")
    public List<BusRecord> getLeastDelay(@PathVariable int num, @PathVariable String ampm,
        @RequestParam(required=false) String sd,
        @RequestParam(required=false) String ed)
    {
    	Matcher m = null;
    	if (sd != null)
    	{
        	m = pat_s.matcher(sd);
        	if (!m.matches()) sd = null;
    	}
    	if (ed != null)
    	{
        	m = pat_s.matcher(ed);
        	if (!m.matches()) ed = null;
    	}

        return model_m.getDelays(num, ampm, sd, ed, true);
    }
    
    

}
