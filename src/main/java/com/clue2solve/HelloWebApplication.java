package com.clue2solve;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class HelloWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloWebApplication.class, args);
	}

	@RequestMapping( method = RequestMethod.GET , value = "/{name}")
	String hello(@PathVariable String name){
		if(StringUtils.isEmpty(name)) name = "Your Name";
		return "Hello "+ name;
	}
}
