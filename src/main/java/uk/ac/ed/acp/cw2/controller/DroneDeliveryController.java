package uk.ac.ed.acp.cw2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.dto.DeliveryPathResponse;
import uk.ac.ed.acp.cw2.dto.MedDispatchRec;
import uk.ac.ed.acp.cw2.service.AvailabilityService;
import uk.ac.ed.acp.cw2.service.PathPlanningService;

import java.util.List;

/**
 * CW2 Drone Delivery Endpoints:
 * POST /api/v1/queryAvailableDrones - Find drones available for multiple dispatches
 * POST /api/v1/calcDeliveryPath - Calculate optimal delivery paths with cost analysis
 * POST /api/v1/calcDeliveryPathAsGeoJson - Generate GeoJSON visualization of delivery path
 */
@RestController
@RequestMapping("/api/v1")
public class DroneDeliveryController {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private PathPlanningService pathPlanningService;

    /**
     * POST /api/v1/queryAvailableDrones
     * Find drones capable of handling all dispatches in the list
     * Dispatches are joined by AND - all records must be matchable with one drone
     */
    @PostMapping("/queryAvailableDrones")
    public ResponseEntity<List<String>> queryAvailableDrones(
            @RequestBody List<MedDispatchRec> dispatches) {

        if (dispatches == null || dispatches.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<String> availableDrones = availabilityService.queryAvailableDrones(dispatches);
        return ResponseEntity.ok(availableDrones);
    }

    /**
     * POST /api/v1/calcDeliveryPath
     * Calculates optimal delivery paths considering drone constraints and no-fly zones
     * Returns cost analysis and detailed flight paths for multiple drones
     */
    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<DeliveryPathResponse> calcDeliveryPath(
            @RequestBody List<MedDispatchRec> dispatches) {

        DeliveryPathResponse response = pathPlanningService.calcDeliveryPath(dispatches);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/calcDeliveryPathAsGeoJson
     * Generate GeoJSON representation of delivery path for single drone scenarios
     */
    @PostMapping(value = "/calcDeliveryPathAsGeoJson",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> calcDeliveryPathAsGeoJson(
            @RequestBody List<MedDispatchRec> dispatches) {

        String geoJson = pathPlanningService.calcDeliveryPathAsGeoJson(dispatches);
        return ResponseEntity.ok(geoJson);
    }
}
