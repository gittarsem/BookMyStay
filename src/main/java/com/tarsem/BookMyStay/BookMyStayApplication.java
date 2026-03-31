package com.tarsem.BookMyStay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookMyStayApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookMyStayApplication.class, args);
	}

}
