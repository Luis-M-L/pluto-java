package com.example.batches.datacrawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class DatacrawlerTasks {

    private static final Logger LOG = LoggerFactory.getLogger(DatacrawlerTasks.class);

    @Value("${persistence.component.basepath}")
    public String PERSISTENCE_BASE;

    @Value("${bitfinex.component.basepath}")
    public String DATASOURCE_BASE;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Scheduled(fixedRate = 300000)
    @Bean
    public Job registerSpotsJob(){
        return jobBuilderFactory.get("registerSpotsJob")
                                .incrementer(new RunIdIncrementer())
                                .flow(step())
                                .end()
                                .build();
    }

    @Bean
    public Step step(){
        return stepBuilderFactory.get("step1")
                                .<byte[], byte[]> chunk(5)
                                .reader(getSpots())
                                .writer(writer())
                                .build();
    }

    @Bean
    public ItemReader<? extends byte[]> getSpots(){
        List<byte[]> spots = new ArrayList<>();
        getParesVigilados().forEach(par -> spots.add(getSpot(par)));
        return new IteratorItemReader<byte[]>(spots.iterator());
    }

    @Bean
    public PersistenciaWriter writer(){
        return new PersistenciaWriter(PERSISTENCE_BASE + "/spot");
    }

    private List<String> getParesVigilados(){
        return Arrays.asList("USDBTC", "USDETH", "ETHBTC");
    }

    private byte[] getSpot(String par){
        HttpRequest request = HttpRequest.newBuilder(URI.create(DATASOURCE_BASE + par)).build();
        HttpResponse<byte[]> response = null;

        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            LOG.error(String.format("Error getting spot for %s", par));
            e.printStackTrace();
        } catch (InterruptedException e) {
            LOG.error(String.format("Error getting spot for %s", par));
            e.printStackTrace();
        }

        return response.body();
    }

}
