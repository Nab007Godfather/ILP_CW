package uk.ac.ed.acp.cw2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ed.acp.cw2.model.*;

import java.util.Arrays;
import java.util.List;

/**
 * Service for fetching data from the external ILP REST service:
 * fetches drones from /drones endpoint
 * fetches service points from /service-points endpoint
 * fetches drone-to-service-point mappings from /drones-for-service-points
 * fetches restricted areas from /restricted-areas endpoint
 */
@Service
public class IlpClientService {

    private static final Logger logger = LoggerFactory.getLogger(IlpClientService.class);

    private final WebClient webClient;

    @Autowired
    public IlpClientService(WebClient ilpWebClient) {
        this.webClient = ilpWebClient;
    }

    // Fetches all drones from REST service

    public List<Drone> getDrones() {
        try {
            logger.debug("Fetching drones from ILP service");

            Drone[] drones = webClient.get()
                    .uri("/drones")
                    .retrieve()
                    .bodyToMono(Drone[].class)
                    .block();

            List<Drone> result = drones != null ? Arrays.asList(drones) : List.of();
            logger.info("Fetched {} drones", result.size());

            return result;

        } catch (Exception e) {
            logger.error("Failed to fetch drones: {}", e.getMessage());
            return List.of();
        }
    }

    // Fetches all service points from REST service
    public List<ServicePoint> getServicePoints() {
        try {
            logger.debug("Fetching service points from ILP service");

            ServicePoint[] servicePoints = webClient.get()
                    .uri("/service-points")
                    .retrieve()
                    .bodyToMono(ServicePoint[].class)
                    .block();

            List<ServicePoint> result = servicePoints != null ? Arrays.asList(servicePoints) : List.of();
            logger.info("Fetched {} service points", result.size());

            return result;

        } catch (Exception e) {
            logger.error("Failed to fetch service points: {}", e.getMessage());
            return List.of();
        }
    }

    // Fetches drone-to-service-point mappings from ILP service
    public List<DroneForServicePoint> getDronesForServicePoints() {
        try {
            logger.debug("Fetching drones-for-service-points from ILP service");

            DroneForServicePoint[] dronesForServicePoints = webClient.get()
                    .uri("/drones-for-service-points")
                    .retrieve()
                    .bodyToMono(DroneForServicePoint[].class)
                    .block();

            List<DroneForServicePoint> result = dronesForServicePoints != null ?
                    Arrays.asList(dronesForServicePoints) : List.of();
            logger.info("Fetched {} drone-service point mappings", result.size());

            return result;

        } catch (Exception e) {
            logger.error("Failed to fetch drones-for-service-points: {}", e.getMessage());
            return List.of();
        }
    }

    // Fetch all restricted areas from ILP service.
    public List<RestrictedArea> getRestrictedAreas() {
        try {
            logger.debug("Fetching restricted areas from ILP service");

            RestrictedArea[] restrictedAreas = webClient.get()
                    .uri("/restricted-areas")
                    .retrieve()
                    .bodyToMono(RestrictedArea[].class)
                    .block();

            List<RestrictedArea> result = restrictedAreas != null ?
                    Arrays.asList(restrictedAreas) : List.of();
            logger.info("Fetched {} restricted areas", result.size());

            return result;

        } catch (Exception e) {
            logger.error("Failed to fetch restricted areas: {}", e.getMessage());
            return List.of();
        }
    }
}
