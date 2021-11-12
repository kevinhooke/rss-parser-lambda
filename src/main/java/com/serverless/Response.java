package com.serverless;

import java.util.ArrayList;
import java.util.List;

public class Response {

	private String message;
	private String firstRun;
	private String lastRun;
	private boolean retrievedFromSource;
	
	private List<String> headlines = new ArrayList<>();

	public Response() {
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public List<String> getHeadlines() {
		return headlines;
	}

	public void setHeadlines(List<String> headlines) {
		this.headlines = headlines;
	}

	public String getFirstRun() {
		return firstRun;
	}

	public void setFirstRun(String firstRun) {
		this.firstRun = firstRun;
	}

	public String getLastRun() {
		return lastRun;
	}

	public void setLastRun(String lastRun) {
		this.lastRun = lastRun;
	}

	public boolean isRetrievedFromSource() {
		return retrievedFromSource;
	}

	public void setRetrievedFromSource(boolean retrievedFromSource) {
		this.retrievedFromSource = retrievedFromSource;
	}
}
