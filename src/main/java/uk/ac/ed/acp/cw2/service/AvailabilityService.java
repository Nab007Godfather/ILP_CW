package uk.ac.ed.acp.cw2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.dto.MedDispatchRec;
import uk.ac.ed.acp.cw2.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityService.class);

    private final IlpClientService ilpClientService;

    @Autowired
    public AvailabilityService(IlpClientService ilpClientService) {
        this.ilpClientService = ilpClientService;
    }

    public List<String> queryAvailableDrones(List<MedDispatchRec> dispatches) {
        if (dispatches == null || dispatches.isEmpty()) {
            logger.debug("No dispatches provided");
            return List.of();
        }

        logger.debug("Checking availability for {} dispatches", dispatches.size());

        List<Drone> allDrones = ilpClientService.getDrones();
        List<DroneForServicePoint> dronesForServicePoints = ilpClientService.getDronesForServicePoints();
        List<ServicePoint> servicePoints = ilpClientService.getServicePoints();

        // Starting with all drones and then narrowing down with each dispatch
        List<String> candidateDrones = new ArrayList<>();
        for (Drone drone : allDrones) {
            candidateDrones.add(drone.getId());
        }

        // Processing each dispatch
        for (MedDispatchRec dispatch : dispatches) {
            candidateDrones = filterDronesForDispatch(
                    candidateDrones, dispatch, allDrones,
                    dronesForServicePoints, servicePoints
            );

            // No drones left so early exit
            if (candidateDrones.isEmpty()) {
                logger.debug("No drones can handle all dispatches");
                return List.of();
            }
        }

        logger.info("Found {} drones available for all dispatches", candidateDrones.size());
        return candidateDrones;
    }

    private List<String> filterDronesForDispatch(
            List<String> candidates,
            MedDispatchRec dispatch,
            List<Drone> allDrones,
            List<DroneForServicePoint> dronesForServicePoints,
            List<ServicePoint> servicePoints) {

        List<String> suitable = new ArrayList<>();

        for (String droneId : candidates) {
            Drone drone = findDroneById(droneId, allDrones);
            if (drone == null) continue;

            if (canHandleDispatch(drone, dispatch, dronesForServicePoints, servicePoints)) {
                suitable.add(droneId);
            }
        }

        return suitable;
    }

    private boolean canHandleDispatch(Drone drone, MedDispatchRec dispatch,
                                      List<DroneForServicePoint> dronesForServicePoints,
                                      List<ServicePoint> servicePoints) {
        DroneCapability capability = drone.getCapability();

        if (capability == null) {
            return false;
        }

        // Checking capacity
        if (dispatch.getRequirements() != null &&
                dispatch.getRequirements().getCapacity() != null) {
            if (capability.getCapacity() < dispatch.getRequirements().getCapacity()) {
                logger.debug("Drone {} fails capacity check", drone.getId());
                return false;
            }
        }

        // Check cooling requirement
        boolean needsCooling = dispatch.getRequirements() != null &&
                Boolean.TRUE.equals(dispatch.getRequirements().getCooling());

        // Check heating requirement
        boolean needsHeating = dispatch.getRequirements() != null &&
                Boolean.TRUE.equals(dispatch.getRequirements().getHeating());

        if (needsCooling && needsHeating) {
            if (!Boolean.TRUE.equals(capability.getCooling()) ||
                    !Boolean.TRUE.equals(capability.getHeating())) {
                logger.debug("Drone {} needs both cooling and heating", drone.getId());
                return false;
            }
        } else if (needsCooling) {
            if (!Boolean.TRUE.equals(capability.getCooling())) {
                logger.debug("Drone {} lacks cooling", drone.getId());
                return false;
            }
        } else if (needsHeating) {
            if (!Boolean.TRUE.equals(capability.getHeating())) {
                logger.debug("Drone {} lacks heating", drone.getId());
                return false;
            }
        }

        // Checking date/time availability
        if (dispatch.getDate() != null && dispatch.getTime() != null) {
            LocalDate date = dispatch.getLocalDate();
            LocalTime time = dispatch.getLocalTime();

            if (!isDroneAvailable(drone.getId(), date, time, dronesForServicePoints)) {
                logger.debug("Drone {} not available at {} {}", drone.getId(), date, time);
                return false;
            }
        }

        // Checking maxCost
        if (dispatch.getRequirements() != null &&
                dispatch.getRequirements().getMaxCost() != null) {

            ServicePoint servicePoint = findServicePointForDrone(
                    drone.getId(), servicePoints, dronesForServicePoints);

            if (servicePoint != null && dispatch.getDelivery() != null) {
                double estimatedCost = estimateDeliveryCost(
                        drone, servicePoint.getLocation(), dispatch.getDelivery());

                if (estimatedCost > dispatch.getRequirements().getMaxCost()) {
                    logger.debug("Drone {} exceeds maxCost: {} > {}",
                            drone.getId(), estimatedCost,
                            dispatch.getRequirements().getMaxCost());
                    return false;
                }
            }
        }

        return true;
    }

    private double estimateDeliveryCost(Drone drone, LngLat servicePoint, LngLat delivery) {
        double distance = DroneNavigation.euclideanDistance(servicePoint, delivery);
        double estimatedMoves = (distance * 2) / DroneNavigation.STEP;

        DroneCapability cap = drone.getCapability();
        return cap.getCostInitial() + cap.getCostFinal()
                + estimatedMoves * cap.getCostPerMove();
    }

    private boolean isDroneAvailable(String droneId, LocalDate date, LocalTime time,
                                     List<DroneForServicePoint> dronesForServicePoints) {
        if (date == null || time == null) {
            return true;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        for (DroneForServicePoint dfsp : dronesForServicePoints) {
            for (DroneForServicePoint.DroneAvailability da : dfsp.getDrones()) {
                if (!da.getId().equals(droneId)) {
                    continue;
                }

                for (DayAvailability availability : da.getAvailability()) {
                    if (availability.getDayOfWeekEnum().equals(dayOfWeek)) {
                        LocalTime fromTime = availability.getFromTime();
                        LocalTime untilTime = availability.getUntilTime();

                        if (!time.isBefore(fromTime) && time.isBefore(untilTime)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

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

    private Drone findDroneById(String droneId, List<Drone> drones) {
        return drones.stream()
                .filter(d -> d.getId().equals(droneId))
                .findFirst()
                .orElse(null);
    }
}
