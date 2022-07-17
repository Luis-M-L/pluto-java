package com.pluto.bitfinex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan("com.example.pluto.entities")
public class BitfinexApplication {

	public static void main(String[] args) {
		SpringApplication.run(BitfinexApplication.class, args);
	}

}
