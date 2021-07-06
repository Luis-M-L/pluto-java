package com.example.batches;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BatchesApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchesApplication.class, args);
	}

}
