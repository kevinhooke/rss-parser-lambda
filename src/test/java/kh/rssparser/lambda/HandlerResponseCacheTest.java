package kh.rssparser.lambda;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.serverless.Response;

import kh.rssparser.RssParser;
import kh.rssparser.model.Channel;
import kh.rssparser.model.Item;
import kh.rssparser.model.Rss;

public class HandlerResponseCacheTest {

	@Test
	public void testFirstCall() throws Exception{

		Handler handler = new Handler();
		
		//mock parseRss to return a mocked Rss result
		RssParser mockParser = mock(RssParser.class);
		//mock response for parseUrl
		Rss mockRss = new Rss();
		//TODO setup mockRss
		Channel mockChannel = new Channel();
		Item mockItem = new Item();
		mockItem.setTitle("test");
		List<Item> items = new ArrayList<>();
		items.add(mockItem);
		mockChannel.setItem(items);
		mockRss.setChannel(mockChannel);
		
		when(mockParser.parseRss("http://test1")).thenReturn(mockRss);
		
		Response responseBody = new Response();
		LocalDateTime now = LocalDateTime.now();
		Rss rss = handler.retrieveContentFromCache(true, now, mockParser, "http://test1", responseBody);
		assertNotNull(rss);
		//if firstLambdaExecution == true this rss should be retrieved from (mock) source, not cache
		assertTrue(responseBody.isRetrievedFromSource());
		
		//now re-retrieve and should be from cache
		responseBody = new Response();
		rss = handler.retrieveContentFromCache(false, now, mockParser, "http://test1", responseBody);
		assertNotNull(rss);
		//if firstLambdaExecution == true this rss should be retrieved from (mock) source, not cache
		assertFalse(responseBody.isRetrievedFromSource());

		//retrieve different rss url
		responseBody = new Response();
		rss = handler.retrieveContentFromCache(false, now, mockParser, "http://test2", responseBody);
		assertNotNull(rss);
		//if firstLambdaExecution == true this rss should be retrieved from (mock) source, not cache
		assertTrue(responseBody.isRetrievedFromSource());

	}

}
