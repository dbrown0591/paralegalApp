package com.paralegal.paralegalApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ParalegalAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParalegalAppApplication.class, args);
	}

}
