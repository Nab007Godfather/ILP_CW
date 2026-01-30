package uk.ac.ed.acp.cw2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.dto.QueryAttribute;
import uk.ac.ed.acp.cw2.model.Drone;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Service for querying drones based on various criteria

@Service
public class QueryService {

    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);

    private final IlpClientService ilpClientService;
    private final ObjectMapper objectMapper;

    @Autowired
    public QueryService(IlpClientService ilpClientService) {
        this.ilpClientService = ilpClientService;
        this.objectMapper = new ObjectMapper();
    }

    // Getting drones by cooling capability
    public List<String> getDronesWithCooling(boolean hasCooling) {
        logger.debug("Querying drones with cooling={}", hasCooling);

        List<Drone> allDrones = ilpClientService.getDrones();

        return allDrones.stream()
                .filter(drone -> drone.getCapability() != null)
                .filter(drone -> drone.getCapability().getCooling() != null)
                .filter(drone -> drone.getCapability().getCooling() == hasCooling)
                .map(Drone::getId)
                .collect(Collectors.toList());
    }

    // Getting drone details by ID.

    public Drone getDroneById(String id) {
        logger.debug("Fetching drone with id={}", id);

        List<Drone> allDrones = ilpClientService.getDrones();

        return allDrones.stream()
                .filter(drone -> drone.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Query drones by a single attribute and value.
    public List<String> queryByPath(String attribute, String value) {
        logger.debug("Querying drones by {}={}", attribute, value);

        List<Drone> allDrones = ilpClientService.getDrones();
        List<String> result = new ArrayList<>();

        for (Drone drone : allDrones) {
            if (matchesSingleAttribute(drone, attribute, "=", value)) {
                result.add(drone.getId());
            }
        }

        logger.info("Found {} drones matching {}={}", result.size(), attribute, value);
        return result;
    }

    // Query drones by multiple attributes with operators

    public List<String> query(List<QueryAttribute> queryAttributes) {
        logger.debug("Querying drones with {} conditions", queryAttributes.size());

        List<Drone> allDrones = ilpClientService.getDrones();
        List<String> result = new ArrayList<>();

        for (Drone drone : allDrones) {
            boolean matchesAll = true;

            for (QueryAttribute qa : queryAttributes) {
                if (!matchesSingleAttribute(drone, qa.getAttribute(),
                        qa.getOperator(), qa.getValue())) {
                    matchesAll = false;
                    break;
                }
            }

            if (matchesAll) {
                result.add(drone.getId());
            }
        }

        logger.info("Found {} drones matching all conditions", result.size());
        return result;
    }

    // Checking if a drone matches a single attribute condition

    private boolean matchesSingleAttribute(Drone drone, String attribute,
                                           String operator, String value) {
        try {
            JsonNode droneNode = objectMapper.valueToTree(drone);
            JsonNode attrValue = getAttributeValue(droneNode, attribute);

            if (attrValue == null || attrValue.isNull()) {
                return false;
            }

            // Handle different types
            if (attrValue.isNumber()) {
                return compareNumeric(attrValue.asDouble(), operator,
                        Double.parseDouble(value));
            } else if (attrValue.isBoolean()) {
                boolean actualValue = attrValue.asBoolean();
                boolean expectedValue = Boolean.parseBoolean(value);

                // Handles both "=" and "!=" for boolean values
                return switch (operator) {
                    case "=" -> actualValue == expectedValue;
                    case "!=" -> actualValue != expectedValue;
                    default -> {
                        logger.warn("Unsupported boolean operator: {}", operator);
                        yield false;
                    }
                };
            } else {
                String actualText = attrValue.asText();
                return switch (operator) {
                    case "=" -> actualText.equals(value);
                    case "!=" -> !actualText.equals(value);
                    default -> {
                        logger.warn("Unsupported string operator: {}", operator);
                        yield false;
                    }
                };
            }

        } catch (Exception e) {
            logger.warn("Error matching attribute {}: {}", attribute, e.getMessage());
            return false;
        }
    }

    // Getting attribute value from JSON node

    private JsonNode getAttributeValue(JsonNode node, String attribute) {
        // Checks capability first
        if (node.has("capability")) {
            JsonNode capabilityNode = node.get("capability");
            if (capabilityNode.has(attribute)) {
                return capabilityNode.get(attribute);
            }
        }

        if (node.has(attribute)) {
            return node.get(attribute);
        }

        return null;
    }

    // Comparing numeric values based on operator

    private boolean compareNumeric(double actual, String operator, double expected) {
        return switch (operator) {
            case "=" -> Math.abs(actual - expected) < 0.0001;
            case "!=" -> Math.abs(actual - expected) >= 0.0001;
            case "<" -> actual < expected;
            case ">" -> actual > expected;
            case "<=" -> actual <= expected;
            case ">=" -> actual >= expected;
            default -> {
                logger.warn("Unknown operator: {}", operator);
                yield false;
            }
        };
    }
}
