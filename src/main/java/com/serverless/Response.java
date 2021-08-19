package com.serverless;

import java.util.ArrayList;
import java.util.List;

public class Response {

	private String message;
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
}
