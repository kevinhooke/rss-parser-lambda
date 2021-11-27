package kh.rssparser.lambda;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.serverless.ApiGatewayResponse;

import kh.rssparser.RssParser;
import kh.rssparser.model.Channel;
import kh.rssparser.model.Item;
import kh.rssparser.model.Rss;

/**
 * Set env var ttl to a non-zero value to mirror Lambda execution environment.
 * 
 * @author kevinhooke
 *
 */
public class HandlerTest {

//	@Before
//    public void setUp() {
//		PowerMockito.mockStatic(System.class);
//		PowerMockito.when(System.getenv(Mockito.eq("ttl"))).thenReturn("30");
//    }
	
	private RssParser mockRssParser(String url, String value) throws Exception{
		//mock parseRss to return a mocked Rss result
		RssParser mockParser = mock(RssParser.class);
		//mock response for parseUrl
		Rss mockRss = buildMockRss(value);
		
		when(mockParser.parseRss(url)).thenReturn(mockRss);
		return mockParser;
	}

	private Rss buildMockRss(String value) {
		Rss mockRss = new Rss();
		Channel mockChannel = new Channel();
		Item mockItem = new Item();
		mockItem.setTitle(value);
		List<Item> items = new ArrayList<>();
		items.add(mockItem);
		mockChannel.setItem(items);
		mockRss.setChannel(mockChannel);
		return mockRss;
	}
	
	@Test
	public void testHandlerRequest_firstCall() throws Exception{
		Handler handler = new Handler();
		//init cache tracking vars to initial state
		Handler.setFirstRun(null);
		Handler.setLastRun(null);
		
		//set mock Parser
		handler.setParser(this.mockRssParser("http://test1", "test"));
		Map<String, Object> event =  new HashMap<>();
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("rss", "http://test1");
		event.put("queryStringParameters", queryStringParameters);
		Context mockContext = mock(Context.class);
		ApiGatewayResponse response = handler.handleRequest(event, mockContext);
		
		assertNotNull(response);
		assertTrue(response.getBody().contains("\"retrievedFromSource\":true"));
	}

	@Test
	public void testHandlerRequest_firstCallFromDesc() throws Exception{
		//TODO
	}
	
	@Test
	public void testHandlerRequest_responseFromCacheOnSubsequentCall() throws Exception{
		Handler handler = new Handler();
		//init cache tracking vars to initial state
		Handler.setFirstRun(null);
		Handler.setLastRun(null);
		
		//set mock Parser
		handler.setParser(this.mockRssParser("http://test1", "test"));
		Map<String, Object> event =  new HashMap<>();
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("rss", "http://test1");
		event.put("queryStringParameters", queryStringParameters);
		Context mockContext = mock(Context.class);
		ApiGatewayResponse response = handler.handleRequest(event, mockContext);
		
		assertNotNull(response);
		assertTrue(response.getBody().contains("\"retrievedFromSource\":true"));

		response = handler.handleRequest(event, mockContext);
		
		assertNotNull(response);
		
		//expected result is false on subsequent calls
		assertTrue(response.getBody().contains("\"retrievedFromSource\":false"));

	}

	@Test
	public void testHandlerRequest_responseFromCacheOnSubsequentCallFromDesc() throws Exception{
		//TODO
	}
	
	@Test
	public void testHandlerRequest_responseFromCacheOnSubsequentCall_differentUrl() throws Exception{
		Handler handler = new Handler();
		//init cache tracking vars to initial state
		Handler.setFirstRun(null);
		Handler.setLastRun(null);
		
		//set mock Parser
		handler.setParser(this.mockRssParser("http://test1", "test"));
		Map<String, Object> event =  new HashMap<>();
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("rss", "http://test1");
		event.put("queryStringParameters", queryStringParameters);
		Context mockContext = mock(Context.class);
		ApiGatewayResponse response = handler.handleRequest(event, mockContext);
		
		assertNotNull(response);
		assertTrue(response.getBody().contains("\"retrievedFromSource\":true"));

		//set params and mock for different rss url
		queryStringParameters.put("rss", "http://test2");
		event.put("queryStringParameters", queryStringParameters);
		handler.setParser(this.mockRssParser("http://test2", "test2"));
		response = handler.handleRequest(event, mockContext);
		
		assertNotNull(response);
		//expected result is true on subsequent calls if url  is different
		assertTrue(response.getBody().contains("\"retrievedFromSource\":true"));

	}
	
	@Test
	public void testHandlerRequest_responseFromCacheOnSubsequentCall_differentUrlFromDesc() throws Exception{
		//TODO
	}
	
}
