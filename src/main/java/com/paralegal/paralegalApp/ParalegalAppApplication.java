package com.paralegal.paralegalApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.paralegal.paralegalApp.Repository")
@EntityScan(basePackages = "com.paralegal.paralegalApp.Model")
//@ComponentScan(basePackages = "com.paralegal.paralegalApp")
public class ParalegalAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParalegalAppApplication.class, args);
	}

}
