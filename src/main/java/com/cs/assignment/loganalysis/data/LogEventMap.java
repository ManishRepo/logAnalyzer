package com.cs.assignment.loganalysis.data;

/**
 * Model class to cache minimum value from the log JSON string for duration calculation
 * @author Manish K Singh
 *
 */
public class LogEventMap {
	
    private String id;

    private String state;

    private long timestamp;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
