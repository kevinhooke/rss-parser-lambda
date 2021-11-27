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
/**
 * 
 * 
 * 
 * 
 * Serverless cli invoke tests:
 * 
 * serverless invoke --function rss-parser-lambda --data '{ "queryStringParameters": {"rss":"http://www.arrl.org/arrl.rss"}}'
 * 
 * serverless invoke --function rss-parser-lambda --data '{ "queryStringParameters": {"rss":"http://www.arrl.org/arrl.rss" }, "pathParameters" : { "item" : "1" } }'
 * 
 * 
 * @author kevinhooke
 *
 */
public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);
	private static LocalDateTime firstRun;
	private static LocalDateTime lastRun;

	private static Map<String, Rss> cache = new HashMap<>();
	
	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> event, Context context) {
		LOG.info("received: {}", event);
		
		ApiGatewayResponse response = null;
		
		RssParser parser = new RssParser();
		Response responseBody = new Response();
		String rssUrl = null;
		String parseFromDescription = null;
		Integer itemIndex = null;
		
		boolean firstLambdaExecution = false;
		if(firstRun == null) {
			firstRun = LocalDateTime.now();
			firstLambdaExecution = true;
		}
		responseBody.setFirstRun(firstRun.toString());
		
		Map<String, String> params = (Map<String, String>)event.get("queryStringParameters");
		Map<String, String> pathParams = (Map<String, String>)event.get("pathParameters");
		
		if(params != null) {
			rssUrl = (String)params.get("rss");
			parseFromDescription = (String)params.get("description");
			
			//TODO: need a page size and max pages param for long responses
			
			if(rssUrl != null && !rssUrl.trim().equals("")) {
				try {
					
					if(pathParams != null) {
						String itemValue = pathParams.get("item");
						if(itemValue != null) {
							itemIndex = Integer.parseInt(itemValue);
						}
						LOG.info("item: " + itemIndex.toString());
					}
					
					Rss rss = this.retrieveContentFromCache(firstLambdaExecution, firstRun, parser, rssUrl, responseBody);
					lastRun = LocalDateTime.now();
					responseBody.setLastRun(lastRun.toString());
					
					if(parseFromDescription == null || parseFromDescription.trim().equals("")) {
					
						List<Item> items = rss.getChannel().getItem();
						if(itemIndex != null) {
							//return text body of headline
							LOG.info("requested item: " + itemIndex.toString() + ", items in list: " + items.size());
							String unparsedDesc = items.get(itemIndex).getDescription();
							String parsedDesc = this.removeNonStandardChars(items.get(itemIndex).getDescription());
							LOG.info("item: " + items.get(itemIndex));
							LOG.info("text unparsed: " + unparsedDesc);
							LOG.info("text parsed: " + parsedDesc);
							responseBody.setText(parsedDesc);
						}
						else {
							//return list of headlines
							for(Item itemText : items) {
								responseBody.getHeadlines().add(this.removeNonStandardChars(itemText.getTitle()));;
							}
						}
						response = ApiGatewayResponse.builder()
								.setStatusCode(200)
								.setObjectBody(responseBody)
								.build();
					}
					else {
						//parse titles from description element
						//TODO: npe here on second retreive
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

	/**
	 * If cache is still valid (time since first retrieved and cached < TTL) then return from cache,
	 * otherwise re-retrieve from source and store to cache.
	 * 
	 * @param parser
	 * @param rssUrl
	 * @param responseBody
	 * @return
	 * @throws URISyntaxException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	Rss retrieveContentFromCache(boolean firstLambdaExecution, LocalDateTime firstRunTime, RssParser parser, String rssUrl, Response responseBody)
			throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
		Rss result = null;
		
		LocalDateTime now = LocalDateTime.now();
		int ttl = 0;
		String ttlValue = System.getenv().get("ttl");
		if(ttlValue != null && !ttlValue.equals("")) {
			ttl = Integer.parseInt(ttlValue);
		}
		LOG.info("TTL: " + ttl);
		
		//TODO: if less than 1 min, this will still retrieve from source - change to seconds
		long secsSinceLastRun = ChronoUnit.SECONDS.between(firstRunTime, now);
		LOG.info("Secs since last run: " + secsSinceLastRun);
		boolean cacheExpired = false;
		if( secsSinceLastRun > ttl * 60 ) {
			cacheExpired = true;
			
			//reset firstRun, otherwise keeps increasing indefinitely
			firstRun = null;
		}
		
		if( cacheExpired || firstLambdaExecution) {
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
