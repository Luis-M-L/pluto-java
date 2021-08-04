package com.example.pluto.instruments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan("com.example.pluto.entities")
public class InstrumentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstrumentsApplication.class, args);
    }

}
