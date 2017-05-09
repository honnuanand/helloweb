package com.clue2solve.spring.batch.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.file.Files;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by anand on 5/9/17.
 */
@Configuration
public class IntegrationConfiguration {
    @Bean
    IntegrationFlow incomingFiles(@Value("${HOME}/clue2solve/data/in") File dir) {
        return IntegrationFlows.from(Files.inboundAdapter(dir)
                .autoCreateDirectory(true)
                .preventDuplicates(true), poller -> poller.poller(spec -> spec.fixedRate(1, TimeUnit.SECONDS)))
                .handle(File.class, (payload, headers) -> {
                    System.out.println("We have  seen " + payload.getAbsolutePath() + " arrive.");
                    return null;
                }).get();

    }
}
