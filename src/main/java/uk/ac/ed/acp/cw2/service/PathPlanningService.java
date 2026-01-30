package uk.ac.ed.acp.cw2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for planning delivery paths for drones
 * calcDeliveryPath: Can split deliveries across multiple drones
 * calcDeliveryPathAsGeoJson: One drone for all deliveries
 * Flight path starts at service point, includes all moves while hover is 1 move
 * Return to service point is a separate delivery in list
 * Hover: Two identical coordinates at delivery point
 * TotalMoves: Includes the hover move (1 move)
 * Groups by date, processes each day separately
 */
@Service
public class PathPlanningService {

    private static final Logger logger = LoggerFactory.getLogger(PathPlanningService.class);

    private final IlpClientService ilpClientService;
    private final AvailabilityService availabilityService;

    @Autowired
    public PathPlanningService(IlpClientService ilpClientService,
                               AvailabilityService availabilityService) {
        this.ilpClientService = ilpClientService;
        this.availabilityService = availabilityService;
    }

    /**
     * Calculates delivery path for dispatches
     * Can split deliveries across multiple drones for optimization
     * Flight structure per drone:
     * ServicePoint → Delivery1 (with hover at end)
     * Delivery1 → Delivery2 (with hover at end)
     * Delivery2 → ServicePoint (return flight, no deliveryId)
     */
    public DeliveryPathResponse calcDeliveryPath(List<MedDispatchRec> dispatches) {
        if (dispatches == null || dispatches.isEmpty()) {
            return new DeliveryPathResponse(0.0, 0, new ArrayList<>());
        }

        logger.info("Planning delivery path for {} dispatches", dispatches.size());

        // Fetches all necessary data
        List<Drone> allDrones = ilpClientService.getDrones();
        List<ServicePoint> servicePoints = ilpClientService.getServicePoints();
        List<DroneForServicePoint> dronesForServicePoints = ilpClientService.getDronesForServicePoints();
        List<RestrictedArea> restrictedAreas = ilpClientService.getRestrictedAreas();

        List<DronePath> allDronePaths = new ArrayList<>();
        double totalCost = 0.0;
        int totalMoves = 0;

        // Groups dispatches by date
        Map<LocalDate, List<MedDispatchRec>> dispatchesByDate = dispatches.stream()
                .filter(d -> d.getDate() != null)
                .collect(Collectors.groupingBy(MedDispatchRec::getLocalDate));

        // Handles dispatches without date
        List<MedDispatchRec> dispatchesWithoutDate = dispatches.stream()
                .filter(d -> d.getDate() == null)
                .collect(Collectors.toList());

        // Processes each date separately
        for (Map.Entry<LocalDate, List<MedDispatchRec>> entry : dispatchesByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<MedDispatchRec> dateDispatches = entry.getValue();

            logger.info("Processing {} dispatches for date {}", dateDispatches.size(), date);

            DeliveryPathResponse dateResponse = planDispatchesForDate(
                    dateDispatches, allDrones, servicePoints,
                    dronesForServicePoints, restrictedAreas);

            allDronePaths.addAll(dateResponse.getDronePaths());
            totalCost += dateResponse.getTotalCost();
            totalMoves += dateResponse.getTotalMoves();
        }

        // Processes dispatches without dates
        if (!dispatchesWithoutDate.isEmpty()) {
            DeliveryPathResponse noDateResponse = planDispatchesForDate(
                    dispatchesWithoutDate, allDrones, servicePoints,
                    dronesForServicePoints, restrictedAreas);

            allDronePaths.addAll(noDateResponse.getDronePaths());
            totalCost += noDateResponse.getTotalCost();
            totalMoves += noDateResponse.getTotalMoves();
        }

        logger.info("Planned paths: {} dispatches, cost={}, moves={}",
                dispatches.size(), totalCost, totalMoves);

        return new DeliveryPathResponse(totalCost, totalMoves, allDronePaths);
    }

    // Plans dispatches for a single date (one drone per dispatch)
    private DeliveryPathResponse planDispatchesForDate(
            List<MedDispatchRec> dispatches,
            List<Drone> allDrones,
            List<ServicePoint> servicePoints,
            List<DroneForServicePoint> dronesForServicePoints,
            List<RestrictedArea> restrictedAreas) {

        List<DronePath> dronePaths = new ArrayList<>();
        double totalCost = 0.0;
        int totalMoves = 0;

        // Processes each dispatch
        for (MedDispatchRec dispatch : dispatches) {
            Drone suitableDrone = findSuitableDrone(dispatch, allDrones, dronesForServicePoints);
            if (suitableDrone == null) {
                logger.warn("No suitable drone found for dispatch {}", dispatch.getId());
                continue;
            }

            ServicePoint servicePoint = findServicePointForDrone(
                    suitableDrone.getId(), servicePoints, dronesForServicePoints);
            if (servicePoint == null) {
                logger.warn("No service point found for drone {}", suitableDrone.getId());
                continue;
            }

            // Builds complete flight for this drone
            List<Delivery> deliveries = new ArrayList<>();
            int flightMoves = 0;

            // ServicePoint → Delivery location
            List<LngLat> outboundPath = calculateSimplePath(
                    servicePoint.getLocation(),
                    dispatch.getDelivery(),
                    restrictedAreas
            );

            // Adds hover at delivery (two identical coordinates)
            outboundPath.add(dispatch.getDelivery());
            outboundPath.add(dispatch.getDelivery());

            Delivery outboundDelivery = new Delivery(dispatch.getId(), outboundPath);
            deliveries.add(outboundDelivery);

            // Counts moves for outbound (including hover)
            int outboundMoves = outboundPath.size() - 1;
            flightMoves += outboundMoves;

            // Delivery location → ServicePoint (return flight)
            List<LngLat> returnPath = calculateSimplePath(
                    dispatch.getDelivery(),
                    servicePoint.getLocation(),
                    restrictedAreas
            );

            // Returns flight has no deliveryId
            Delivery returnDelivery = new Delivery(-1, returnPath);
            deliveries.add(returnDelivery);

            // Counts moves for return
            int returnMoves = returnPath.size() - 1;
            flightMoves += returnMoves;

            // Creates drone path
            DronePath dronePath = new DronePath(suitableDrone.getId(), deliveries);
            dronePaths.add(dronePath);

            // Calculates cost
            double flightCost = calculateFlightCost(suitableDrone, flightMoves, 1);

            totalMoves += flightMoves;
            totalCost += flightCost;

            logger.debug("Drone {} delivers dispatch {}: {} moves, cost {}",
                    suitableDrone.getId(), dispatch.getId(), flightMoves, flightCost);
        }

        return new DeliveryPathResponse(totalCost, totalMoves, dronePaths);
    }

    // Calculates total cost for a flight
    private double calculateFlightCost(Drone drone, int totalMoves, int numDeliveries) {
        DroneCapability cap = drone.getCapability();

        // Total flight cost
        double flightCost = cap.getCostInitial() +
                cap.getCostFinal() +
                (totalMoves * cap.getCostPerMove());

        return flightCost;
    }

    // Generates GeoJSON LineString for delivery path.
    public String calcDeliveryPathAsGeoJson(List<MedDispatchRec> dispatches) {
        DeliveryPathResponse response = calcDeliveryPath(dispatches);

        if (response.getDronePaths().isEmpty()) {
            return "{\"type\":\"FeatureCollection\",\"features\":[]}";
        }

        // Takes first drone path
        DronePath dronePath = response.getDronePaths().get(0);

        StringBuilder geoJson = new StringBuilder();
        geoJson.append("{\"type\":\"LineString\",\"coordinates\":[");

        boolean first = true;
        for (Delivery delivery : dronePath.getDeliveries()) {
            for (LngLat point : delivery.getFlightPath()) {
                if (!first) geoJson.append(",");
                geoJson.append(String.format("[%.6f,%.6f]", point.getLng(), point.getLat()));
                first = false;
            }
        }

        geoJson.append("]}");
        return geoJson.toString();
    }

    // Finds suitable drone for a dispatch using availability service.
    private Drone findSuitableDrone(MedDispatchRec dispatch, List<Drone> allDrones,
                                    List<DroneForServicePoint> dronesForServicePoints) {
        List<String> availableDrones = availabilityService.queryAvailableDrones(
                List.of(dispatch));

        for (String droneId : availableDrones) {
            for (Drone drone : allDrones) {
                if (drone.getId().equals(droneId)) {
                    return drone;
                }
            }
        }

        return null;
    }

    // Find service point where drone is stationed.
    private ServicePoint findServicePointForDrone(String droneId,
                                                  List<ServicePoint> servicePoints,
                                                  List<DroneForServicePoint> dronesForServicePoints) {
        for (DroneForServicePoint dfsp : dronesForServicePoints) {
            boolean hasDrone = dfsp.getDrones().stream()
                    .anyMatch(da -> da.getId().equals(droneId));

            if (hasDrone) {
                return servicePoints.stream()
                        .filter(sp -> sp.getId().equals(dfsp.getServicePointId()))
                        .findFirst()
                        .orElse(null);
            }
        }

        return null;
    }

    // Calculates simple path using greedy approach.
    private List<LngLat> calculateSimplePath(LngLat start, LngLat goal,
                                             List<RestrictedArea> restrictedAreas) {
        List<LngLat> path = new ArrayList<>();
        path.add(start); // First coordinate is starting position

        LngLat current = start;
        int maxIterations = 10000;
        int iterations = 0;

        while (!DroneNavigation.isClose(current, goal) && iterations < maxIterations) {
            double bestAngle = findBestAngle(current, goal, restrictedAreas);
            LngLat next = DroneNavigation.nextPosition(current, bestAngle);
            path.add(next);
            current = next;
            iterations++;
        }

        if (iterations >= maxIterations) {
            logger.warn("Path calculation hit max iterations");
        }

        return path;
    }

    // Find best angle to move towards goal while avoiding obstacles.
    private double findBestAngle(LngLat current, LngLat goal,
                                 List<RestrictedArea> restrictedAreas) {
        double bestAngle = 0;
        double minDistance = Double.MAX_VALUE;

        for (double angle : DroneNavigation.ALLOWED_ANGLES) {
            LngLat next = DroneNavigation.nextPosition(current, angle);

            // Skips if it is no-fly zone
            if (isInNoFlyZone(next, restrictedAreas)) {
                continue;
            }

            double distance = DroneNavigation.euclideanDistance(next, goal);

            if (distance < minDistance) {
                minDistance = distance;
                bestAngle = angle;
            }
        }

        return bestAngle;
    }

    // Checks if position is in a no-fly zone
    private boolean isInNoFlyZone(LngLat position, List<RestrictedArea> restrictedAreas) {
        for (RestrictedArea area : restrictedAreas) {
            if (area.isNoFlyZone()) {
                Region region = new Region(area.getName(), area.getVertices());
                if (PointInRegion.isPointInRegion(position, region)) {
                    return true;
                }
            }
        }
        return false;
    }
}
