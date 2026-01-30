package uk.ac.ed.acp.cw2.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.model.LngLat;
import uk.ac.ed.acp.cw2.service.DroneNavigation;
import uk.ac.ed.acp.cw2.service.PointInRegion;

/**
 * CW1 Endpoints:
 * GET /api/v1/uid - Return student ID
 * POST /api/v1/distanceTo - Calculate distance between positions
 * POST /api/v1/isCloseTo - Check if positions are close
 * POST /api/v1/nextPosition - Calculate next position
 * POST /api/v1/isInRegion - Check if point is in region
 */
@RestController
@RequestMapping("/api/v1")
public class CoreRestController {

    /**
     * GET /api/v1/uid
     */
    @GetMapping("/uid")
    public ResponseEntity<String> getUid() {
        return ResponseEntity.ok("s2581854");
    }

    /**
     * POST /api/v1/distanceTo
     * Calculate Euclidean distance between two positions
     */
    @PostMapping("/distanceTo")
    public ResponseEntity<?> distanceTo(@Valid @RequestBody DistanceRequest request,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors() || !request.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            double distance = DroneNavigation.euclideanDistance(
                    request.getPosition1(),
                    request.getPosition2()
            );
            return ResponseEntity.ok(distance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * POST /api/v1/isCloseTo
     * Check if two positions are close (< 0.00015 degrees)
     */
    @PostMapping("/isCloseTo")
    public ResponseEntity<?> isCloseTo(@Valid @RequestBody DistanceRequest request,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors() || !request.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            boolean isClose = DroneNavigation.isClose(
                    request.getPosition1(),
                    request.getPosition2()
            );
            return ResponseEntity.ok(isClose);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * POST /api/v1/nextPosition
     * Calculate next position based on start and angle
     */
    @PostMapping("/nextPosition")
    public ResponseEntity<?> nextPosition(@Valid @RequestBody NextPositionRequest request,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors() || !request.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            LngLat nextPos = DroneNavigation.nextPosition(
                    request.getStart(),
                    request.getAngle()
            );
            return ResponseEntity.ok(nextPos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * POST /api/v1/isInRegion
     * Check if a point is inside a region
     */
    @PostMapping("/isInRegion")
    public ResponseEntity<?> isInRegion(@Valid @RequestBody IsInRegionRequest request,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors() || !request.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            boolean isInside = PointInRegion.isPointInRegion(
                    request.getPosition(),
                    request.getRegion()
            );
            return ResponseEntity.ok(isInside);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
