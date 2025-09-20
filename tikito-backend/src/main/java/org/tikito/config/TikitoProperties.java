package org.tikito.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tikito")
@Getter
@Setter
public class TikitoProperties {
    private FinnubProperties finnhub;

    @Getter
    @Setter
    public static class FinnubProperties {
        private String token;
    }
}
