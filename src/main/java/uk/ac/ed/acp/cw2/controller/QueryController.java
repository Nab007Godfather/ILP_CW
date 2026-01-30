package uk.ac.ed.acp.cw2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.dto.QueryAttribute;
import uk.ac.ed.acp.cw2.model.Drone;
import uk.ac.ed.acp.cw2.service.QueryService;

import java.util.List;

/**
 * CW2 Query Endpoints:
 * GET /api/v1/dronesWithCooling/{state} - Filter drones by cooling capability
 * GET /api/v1/droneDetails/{id} - Retrieve detailed drone information (404 if invalid)
 * GET /api/v1/queryAsPath/{attribute}/{value} - Single attribute path-based query
 * POST /api/v1/query - Multi-attribute query with operators (AND conjunction)
 */
@RestController
@RequestMapping("/api/v1")
public class QueryController {

    @Autowired
    private QueryService droneQueryService;

    /**
     * GET /api/v1/dronesWithCooling/{state}
     * Return drone IDs supporting cooling or not
     */
    @GetMapping("/dronesWithCooling/{state}")
    public ResponseEntity<List<String>> dronesWithCooling(@PathVariable boolean state) {
        List<String> droneIds = droneQueryService.getDronesWithCooling(state);
        return ResponseEntity.ok(droneIds);
    }

    /**
     * GET /api/v1/droneDetails/{id}
     * Return complete drone details for given ID
     */
    @GetMapping("/droneDetails/{id}")
    public ResponseEntity<Drone> droneDetails(@PathVariable String id) {
        Drone drone = droneQueryService.getDroneById(String.valueOf(id));
        if (drone == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(drone);
    }

    /**
     * GET /api/v1/queryAsPath/{attribute}/{value}
     * Single attribute equality query via URL path variables
     */
    @GetMapping("/queryAsPath/{attribute}/{value}")
    public ResponseEntity<List<String>> queryAsPath(@PathVariable String attribute,
                                                    @PathVariable String value) {
        List<String> droneIds = droneQueryService.queryByPath(attribute, value);
        return ResponseEntity.ok(droneIds);
    }

    /**
     * POST /api/v1/query
     * Multi-attribute query with operators (=, !=, <, >) joined by AND
     * Supports numerical comparisons and boolean conversions
     */
    @PostMapping("/query")
    public ResponseEntity<List<String>> query(@RequestBody List<QueryAttribute> queryAttributes) {
        if (queryAttributes == null || queryAttributes.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<String> droneIds = droneQueryService.query(queryAttributes);
        return ResponseEntity.ok(droneIds);
    }

}
