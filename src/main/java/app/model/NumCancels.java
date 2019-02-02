package app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//
// Struct to hold info about a bus and its # cancellations, e.g. #256 : 11 cancellations.
//

@JsonIgnoreProperties(ignoreUnknown = false)
public class NumCancels {

	private int busnumber;
	private int numcancels;
	
	public NumCancels(int busnum, int cancels)
	{
		busnumber = busnum;
		numcancels = cancels;
	}
	
	// getters - needed by Jackson
	public int getbusnumber() { return busnumber; }
	public int getnumcancels() { return numcancels; }
	
	@Override
	public String toString()
	{
		return "#" + busnumber + ": [" + numcancels + "]";
	}
}
