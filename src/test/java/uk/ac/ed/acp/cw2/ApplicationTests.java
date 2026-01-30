package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import uk.ac.ed.acp.cw2.controller.CoreRestController;
import uk.ac.ed.acp.cw2.controller.DroneDeliveryController;
import uk.ac.ed.acp.cw2.controller.QueryController;
import uk.ac.ed.acp.cw2.service.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Basic Spring Boot application context tests.
 *
 * Verifies:
 * - Application context loads successfully
 * - All controllers are properly initialized
 * - All services are properly initialized
 * - Bean wiring is correct
 */
@SpringBootTest
class ApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // Verify application context loads successfully
        assertNotNull(applicationContext, "Application context should load");
    }

    @Test
    void controllersAreInitialized() {
        // Verify all controllers are present
        assertNotNull(applicationContext.getBean(CoreRestController.class),
                "CoreRestController should be initialized");
        assertNotNull(applicationContext.getBean(QueryController.class),
                "QueryController should be initialized");
        assertNotNull(applicationContext.getBean(DroneDeliveryController.class),
                "DroneDeliveryController should be initialized");
    }

    @Test
    void servicesAreInitialized() {
        // Verifies if all Spring managed services are present
        assertNotNull(applicationContext.getBean(IlpClientService.class),
                "IlpClientService should be initialized");
        assertNotNull(applicationContext.getBean(QueryService.class),
                "QueryService should be initialized");
        assertNotNull(applicationContext.getBean(AvailabilityService.class),
                "AvailabilityService should be initialized");
        assertNotNull(applicationContext.getBean(PathPlanningService.class),
                "PathPlanningService should be initialized");
    }

    @Test
    void webClientIsConfigured() {
        // Verifies if WebClient bean is configured
        assertNotNull(applicationContext.getBean("ilpWebClient"),
                "ILP WebClient should be configured");
    }

    @Test
    void ilpEndpointIsConfigured() {
        // Verifies if ILP endpoint bean is configured
        assertNotNull(applicationContext.getBean("ilpEndpoint"),
                "ILP endpoint URL should be configured");
    }
}
