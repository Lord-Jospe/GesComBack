package com.GesCom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GesComApplication {

	public static void main(String[] args) {
		SpringApplication.run(GesComApplication.class, args);
	}

}
