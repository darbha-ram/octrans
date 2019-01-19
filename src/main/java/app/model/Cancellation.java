package app.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * Table in Azure SQL DB
 * 
 * CREATE TABLE buscancellations (
 *     busnumber           smallint       not null,
 *     busname             varchar(64)    not null,
 *     tweettime           smalldatetime,
 *     cancelledstarttime  smalldatetime,
 *     cancelledendtime    smalldatetime,
 *     cancelledstartloc   varchar(32),
 *     cancelledendloc     varchar(32),
 *     nextminutes         smallint);
 *     
 *     For Java8 LocalDateTime parsing from String:
 *     https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
 */

@JsonIgnoreProperties(ignoreUnknown = false)
public class Cancellation {

	// Format of dates as stored in the OC Transpo Tweets DB in Azure
	private static final DateTimeFormatter dtf_s = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss.S");
			
    private int            busnumber;
    private String         busname;
    
    private LocalDateTime  starttime;
    private String         startloc;

    private int            nextminutes;
    
    public Cancellation(
        int num,
        String name,
        String stime,
        String sloc,
        int nextmin
        )
    {
    	busnumber    = num;
    	busname      = name;
    	starttime    = LocalDateTime.parse(stime, dtf_s);
    	startloc     = sloc;
    	nextminutes  = nextmin;
    }

    // getters -- needed by Jackson
    public int     getbusnumber() { return busnumber; }
    public String  getbusname() { return busname; }
    public LocalDateTime  getstarttime() { return starttime; }
    public String  getstartloc() { return startloc; }
    public int     getnextminutes()  { return nextminutes; }

    @Override
    public String toString() {
        return String.format(
                "Cancellation[%d %s at %s [next: %d min]",
                busnumber, busname, starttime, nextminutes);
    }
}

