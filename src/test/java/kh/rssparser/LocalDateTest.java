package kh.rssparser;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class LocalDateTest {

	public static void main(String[] args){
		Result result = JUnitCore.runClasses(LocalDateTest.class);
	}
	
	@Test
	public void testLocalDateToString() {

		LocalDate now = LocalDate.now();
		
		System.out.println("now: " + now.toString());
		
	}

}
