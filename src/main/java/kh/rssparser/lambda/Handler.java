package kh.rssparser.lambda;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;
import com.serverless.Response;

import kh.rssparser.RssParser;
import kh.rssparser.model.Item;
import kh.rssparser.model.Rss;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);

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
					Rss rss = parser.parseRss(rssUrl);
					
					if(parseFromDescription == null || parseFromDescription.trim().equals("")) {
					
						List<Item> items = rss.getChannel().getItem();
						for(Item itemText : items) {
							responseBody.getHeadlines().add(itemText.getTitle());
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
								responseBody.getHeadlines().add(title);
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
}
