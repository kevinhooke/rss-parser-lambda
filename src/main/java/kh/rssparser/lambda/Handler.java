package kh.rssparser.lambda;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.serverless.ApiGatewayResponse;
import com.serverless.Response;

import kh.rssparser.RssParser;
import kh.rssparser.model.Item;
import kh.rssparser.model.Rss;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);
	private static LocalDateTime firstRun;
	private static LocalDateTime lastRun;

	private static Map<String, Rss> cache = new HashMap<>();
	
	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);
		
		ApiGatewayResponse response = null;
		
		RssParser parser = new RssParser();
		Response responseBody = new Response();
		String rssUrl = null;
		String parseFromDescription = null;
		
		Map<String, String> params = (Map<String, String>)input.get("queryStringParameters");
		
		if(params != null) {
			rssUrl = (String)params.get("rss");
			parseFromDescription = (String)params.get("description");
			
			//TODO: need a page size and max pages param for long responses
			
			if(rssUrl != null && !rssUrl.trim().equals("")) {
				try {
					
					Rss rss = this.retrieveContentFromCache(parser, rssUrl, responseBody);
					responseBody.setFirstRun(firstRun.toString());
					responseBody.setLastRun(lastRun.toString());
					
					if(parseFromDescription == null || parseFromDescription.trim().equals("")) {
					
						List<Item> items = rss.getChannel().getItem();
						for(Item itemText : items) {
							responseBody.getHeadlines().add(this.removeNonStandardChars(itemText.getTitle()));;
						}
						
						response = ApiGatewayResponse.builder()
								.setStatusCode(200)
								.setObjectBody(responseBody)
								.build();
					}
					else {
						//parse titles from description element
						List<Item> items = rss.getChannel().getItem();
						
						//TODO: change this to use a page size param
						for(int i=0; i < 1; i++) {
							Item item = items.get(i);
							String description = item.getDescription();
							System.out.println("Description: " + description);
							List<String> titles = parser.extractStringsFromDescription(description);
							for(String title : titles) {
								System.out.println("... title: " + title);
								responseBody.getHeadlines().add((this.removeNonStandardChars(title)));
							}
						}
						
						response = ApiGatewayResponse.builder()
								.setStatusCode(200)
								.setObjectBody(responseBody)
								.build();	
					}
	
				} catch (Exception e) {
					e.printStackTrace();
	
					response = ApiGatewayResponse.builder()
							.setStatusCode(500)
							.setObjectBody(responseBody)
							.build();
				}
			}
			else {
				response = ApiGatewayResponse.builder()
						.setStatusCode(400)
						.setObjectBody(responseBody)
						.build();
			}

		}
		else {
			response = ApiGatewayResponse.builder()
					.setStatusCode(400)
					.setObjectBody(responseBody)
					.build();
		}
		
		return response;
	}

	Rss retrieveContentFromCache(RssParser parser, String rssUrl, Response responseBody)
			throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
		Rss result = null;
		
		boolean firstLambdaExecution = false;
		if(firstRun == null) {
			firstRun = LocalDateTime.now();
			firstLambdaExecution = true;
		}
		
		lastRun = LocalDateTime.now();
		
		int ttl = 0;
		String ttlValue = System.getenv().get("ttl");
		if(ttlValue != null && !ttlValue.equals("")) {
			ttl = Integer.parseInt(ttlValue);
		}
		LOG.info("TTL: " + ttl);
		
		//TODO: if less than 1 min, this will still retrieve from source - change to seconds
		long minsSinceLastRun = ChronoUnit.MINUTES.between(firstRun, lastRun);
		LOG.info("Mins since last run: " + minsSinceLastRun);
		
		if( minsSinceLastRun > ttl || firstLambdaExecution) {
			LOG.info("Cache expired, retrieving from source");
			responseBody.setRetrievedFromSource(true);
			result = parser.parseRss(rssUrl);
			this.addToCache(rssUrl, result);
		}
		else {
			LOG.info("Cache valid, returning from cache");
			responseBody.setRetrievedFromSource(false);
			result = getFromCache(rssUrl);
		}
		
		
		return result;
	}
	
	
	private void addToCache(String rssUrl, Rss result) {
		cache.put(rssUrl, result);
	}

	
	/**
	 * Retrieves response from cache using the url as a key.
	 * 
	 * @param rssUrl
	 * @return
	 */
	private Rss getFromCache(String rssUrl) {
		
		return cache.get(rssUrl);
	}

	String removeNonStandardChars(String text) {
		String result = text.replaceAll("“", "");
		result = result.replaceAll("”", "");

		return result;
		}
	}
