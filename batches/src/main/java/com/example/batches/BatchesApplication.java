package com.example.batches;

import com.example.batches.assetmanager.AssetManagerTasks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class BatchesApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchesApplication.class, args);
	}

	@Scheduled(fixedRate = 60000)
	public void crawl() {
		//DatacrawlerTasks.registerSpots();
	}

	@Scheduled(fixedRate = 60000)
	public void checkPositions() {
		AssetManagerTasks.rebalance();
	}
}
