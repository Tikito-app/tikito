package org.tikito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public final class TikitoApplication {

    public static void main(final String[] args) {
        SpringApplication.run(TikitoApplication.class, args);
    }

}
