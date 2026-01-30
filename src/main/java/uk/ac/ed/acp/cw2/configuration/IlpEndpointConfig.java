package uk.ac.ed.acp.cw2.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class IlpEndpointConfig {

    private static final Logger logger = LoggerFactory.getLogger(IlpEndpointConfig.class);

    private static final String DEFAULT_ILP_ENDPOINT =
            "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net";

    @Bean
    public String ilpEndpoint() {
        String endpoint = System.getenv("ILP_ENDPOINT");

        if (endpoint == null || endpoint.trim().isEmpty()) {
            endpoint = DEFAULT_ILP_ENDPOINT;
            logger.info("Using default ILP endpoint: {}", endpoint);
        } else {
            logger.info("Using ILP_ENDPOINT from environment: {}", endpoint);
        }

        // Remove trailing slash if present
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        return endpoint;
    }

    /**
     * @param ilpEndpoint the base URL for the ILP service
     * @return configured WebClient instance
     */
    @Bean
    public WebClient ilpWebClient(String ilpEndpoint) {
        logger.info("Creating WebClient with base URL: {}", ilpEndpoint);

        return WebClient.builder()
                .baseUrl(ilpEndpoint)
                .defaultHeader("User-Agent", "ILP-CW2-RestService/2.0")
                .build();
    }
}
