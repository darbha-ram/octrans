package app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//
// Struct to hold info about a bus and its # cancellations, e.g. #256 : 11 cancellations.
//

@JsonIgnoreProperties(ignoreUnknown = false)
public class BusRecord {

	private int busnumber;
	private int value;
	
	public BusRecord(int busnum, int cancels)
	{
		busnumber = busnum;
		value = cancels;
	}
	
	// getters - needed by Jackson
	public int getbusnumber() { return busnumber; }
	public int getvalue() { return value; }
	
	@Override
	public String toString()
	{
		return "#" + busnumber + ": [" + value + "]";
	}
}
