package com.cs.assignment.loganalysis.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The domain model that will persist into the persistent storage
 * @author Manish K Singh
 *
 */
@Entity
@Table(name = "LogAlert")
public class LogEventPersistModel {
	
	@Id
	@Column(name = "id")
	private String id;
	@Column(name = "type")
	private String type;
	@Column(name = "host")
	private String host;
	@Column(name = "duration")
	private long duration;
	@Column
	private boolean alert;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isAlert() {
		return alert;
	}

	public void setAlert(boolean alert) {
		this.alert = alert;
	}

}
