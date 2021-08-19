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
		
		RssParser parser = new RssParser();
		Response responseBody = new Response();

		try {
			Rss rss = parser.parseRss("TODO: pass as param");
			
			List<Item> items = rss.getChannel().getItem();
			for(Item itemText : items) {
				responseBody.getHeadlines().add(itemText.getTitle());
			}
		} catch (Exception e) {
			e.printStackTrace();
			//TODO add error response
		}
		
		
		
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
				.build();
	}
}
