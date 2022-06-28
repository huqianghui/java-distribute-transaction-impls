package com.seattle.msready.chained.transaction.rest;

import java.util.concurrent.atomic.AtomicLong;

import com.seattle.msready.chained.transaction.service.FillTables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RestService {

	private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    
    @Autowired
    private FillTables fillTables;
    
	@RequestMapping("/fill")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		
		fillTables.fill();
		
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}
	
	public static class Greeting {

	    private final long id;
	    private final String content;

	    public Greeting(long id, String content) {
	        this.id = id;
	        this.content = content;
	    }

	    public long getId() {
	        return id;
	    }

	    public String getContent() {
	        return content;
	    }
	}

}
