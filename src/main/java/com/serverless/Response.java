package com.serverless;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Response {

	private final String message;
	private List<String> headlines = new ArrayList<>();
	private final Map<String, Object> input;

	public Response(String message, Map<String, Object> input) {
		this.message = message;
		this.input = input;
	}

	public String getMessage() {
		return this.message;
	}

	public Map<String, Object> getInput() {
		return this.input;
	}

	public List<String> getHeadlines() {
		return headlines;
	}

	public void setHeadlines(List<String> headlines) {
		this.headlines = headlines;
	}
}
