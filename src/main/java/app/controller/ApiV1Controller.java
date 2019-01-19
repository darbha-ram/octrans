package app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import app.model.Cancellation;
import app.model.OcTranspoModel;


/*
 * REST API
 * 
 * base: /ocapi/v1/
 *     bus/<number>               -- all cancellations of bus
 *     bus/<number>/am or /pm     -- cancellations of bus in morning or evening
 *     bus/<number>?sd=xxx&ed=yyy -- cancellations of bus between start & end dates
 *     
 *     most/<n>                   -- 'n' buses with the most cancellations
 *     least/<n>                  -- 'n' buses with the least cancellations
 *         .. ?sd=xxx&ed=yyy      -- 'n' most or least cancelled buses in given time frame 
 */

@RestController
public class ApiV1Controller {

    public static final String base_s = "/ocapi/v1/";
    public static final String am_s = "am";
    public static final String pm_s = "pm";

    @Autowired
    private OcTranspoModel model_m;
    
    @RequestMapping(base_s)
    public String getIndex()
    {
        return "Root";
    }
    
    // use non capturing group in ampm regex
    @RequestMapping(base_s + "bus/{busnum:[\\d]+}/{ampm:^(?:am|pm|all)$}")
    public List<Cancellation> getBus(@PathVariable int busnum, @PathVariable String ampm)
    {
    	return model_m.getBus(busnum, ampm, null, null);
    }
    
    
    @RequestMapping(base_s + "most/{num:[\\d]+}/{ampm:^(?:am|pm|all)$}")
    public String getMost(@PathVariable int num, @PathVariable String ampm)
    {

        return "Most  " + num + " received ";
    }
    
    
    @RequestMapping(base_s + "least/{num:[\\d]+}/{ampm:^(?:am|pm|all)$}")
    public String getLeast(@PathVariable int num, @PathVariable String ampm)
    {

        return "Least  " + num + " received  ";
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    /*
    private String getTime(final String ampm)
    {
        if ((ampm == null) || (ampm.isEmpty()))
        	return "both";
        else if (ampm.equalsIgnoreCase(am_s))
        	return am_s;
        else if (ampm.equalsIgnoreCase(pm_s))
        	return pm_s;
        else return null;    	
    }
    */
    

}