# ILP 2025 Coursework 2 - Drone Delivery REST Service

## 📋 Project Overview
A comprehensive Spring Boot REST service for drone-based medical delivery management, extending CW1 with advanced drone querying, availability checking, and delivery path planning capabilities as specified in the ILP 2025 CW2 requirements.

**Course**: Informatics Large Practical (ILP) 2025 
**Student ID**: s1234567 *(Replace with your actual student ID)*

## 🚀 Quick Start

### Prerequisites
- **Java 17** or later
- **Maven 3.8+**
- **Docker** (for containerization)
- **curl** or **Postman** for API testing

### Running the Application

#### Using Maven (Development)
```bash
# Build the application
mvn clean package

# Run the application
java -jar target/ilp-rest-service-*.jar
```

# Service available at http://localhost:8080
Option 2: Using Docker (Production)
bash
# Build Docker image
docker build -t ilp-cw2-service .

# Run container
docker run -p 8080:8080 ilp-cw2-service

# Test the service
curl http://localhost:8080/api/v1/uid
📁 Project Structure
text
ilp-submission-2/
├── src/
│   ├── main/
│   │   ├── java/uk/ac/ed/acp/cw2/
│   │   │   ├── IlpRestServiceApplication.java      # Spring Boot entry point
│   │   │   ├── configuration/
│   │   │   │   └── IlpEndpointConfig.java          # External ILP service configuration
│   │   │   ├── controller/
│   │   │   │   ├── CoreRestController.java         # CW1 endpoints
│   │   │   │   ├── QueryController.java            # Drone query endpoints
│   │   │   │   └── DroneDeliveryController.java    # Delivery planning endpoints
│   │   │   ├── dto/                                # Data transfer objects
│   │   │   │   ├── MedDispatchRec.java
│   │   │   │   ├── DeliveryRequirements.java
│   │   │   │   ├── QueryAttribute.java
│   │   │   │   ├── DeliveryPathResponse.java
│   │   │   │   └── CW1 request/response DTOs
│   │   │   ├── model/                              # Domain models
│   │   │   │   ├── Drone.java & DroneCapability.java
│   │   │   │   ├── ServicePoint.java
│   │   │   │   ├── RestrictedArea.java
│   │   │   │   ├── DroneForServicePoint.java
│   │   │   │   ├── LngLat.java & Region.java
│   │   │   │   └── DayAvailability.java
│   │   │   ├── service/                            # Business logic
│   │   │   │   ├── IlpClientService.java           # External ILP API client
│   │   │   │   ├── QueryService.java               # Drone query operations
│   │   │   │   ├── AvailabilityService.java        # Drone availability checks
│   │   │   │   ├── PathPlanningService.java        # Delivery path calculation
│   │   │   │   ├── DroneNavigation.java            # Utility class (static methods)
│   │   │   │   └── PointInRegion.java              # Utility class (static methods)
│   │   │   └── exception/
│   │   │       └── RestExceptionHandler.java       # Global exception handling
│   │   └── resources/
│   │       └── application.properties              # Application configuration
│   └── test/                                       # Comprehensive test suite
├── pom.xml                                         # Maven dependencies
├── Dockerfile                                      # Container configuration
├── ilp_submission_image.tar                        # Docker image (for submission)
└── README.md                                       # This file
🌐 API Endpoints
All endpoints are prefixed with /api/v1/

CW1 Core Endpoints (Maintained)
Student ID
GET /uid
Response: s1234567 (plain text)

Distance Calculation
POST /distanceTo
Request: Two LngLat positions
Response: Euclidean distance in degrees

Proximity Check
POST /isCloseTo
Request: Two LngLat positions
Response: true or false (within 0.00015° threshold)

Next Position Calculation
POST /nextPosition
Request: Start position and angle (16 compass directions)
Response: Next position coordinates

Region Containment Check
POST /isInRegion
Request: Position and closed polygon region
Response: true or false

CW2 Query Endpoints
Static Queries
Drones with Cooling Capability
GET /dronesWithCooling/{state}
Parameters: state (true/false)
Response: Array of drone IDs with specified cooling capability

Drone Details
GET /droneDetails/{id}
Parameters: id (String drone ID)
Response: Complete drone object or 404 if not found
*Exception to 200-only rule: Returns 404 for invalid IDs*

Dynamic Queries
Single Attribute Path Query
GET /queryAsPath/{attribute}/{value}
Parameters: Attribute name and value as path variables
Response: Array of drone IDs matching the attribute equality

Multi-Attribute Query
POST /query
Request: Array of query attributes with operators (=, !=, <, >, <=, >=)
Response: Array of drone IDs matching ALL conditions (AND logic)

CW2 Delivery Planning Endpoints
Available Drones Query
POST /queryAvailableDrones
Request: Array of MedDispatchRec objects
Response: Array of drone IDs capable of handling ALL dispatches
Note: Dispatches joined by AND logic

Delivery Path Calculation
POST /calcDeliveryPath
Request: Array of MedDispatchRec objects
Response: Structured path data with costs and flight paths

json
{
  "totalCost": 1234.44,
  "totalMoves": 12111,
  "dronePaths": [
    {
      "droneId": "4",
      "deliveries": [
        {
          "deliveryId": 123,
          "flightPath": [
            {"lng": -3.186358, "lat": 55.944680},
            {"lng": -3.186359, "lat": 55.944680}
          ]
        }
      ]
    }
  ]
}
GeoJSON Delivery Path
POST /calcDeliveryPathAsGeoJson
Request: Array of MedDispatchRec objects
Response: GeoJSON LineString compatible with geojson.io
Guarantee: All deliveries can be made with single drone

🧪 Testing
Run All Tests
bash
mvn test
Manual Testing Examples
bash
# Test drone queries
curl http://localhost:8080/api/v1/dronesWithCooling/true

# Test multi-attribute query
curl -X POST http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '[
    {"attribute": "capacity", "operator": ">", "value": "5.0"},
    {"attribute": "cooling", "operator": "=", "value": "true"}
  ]'

# Test delivery planning
curl -X POST http://localhost:8080/api/v1/calcDeliveryPath \
  -H "Content-Type: application/json" \
  -d '[
    {
      "id": 123,
      "date": "2025-12-22",
      "time": "14:30",
      "requirements": {
        "capacity": 0.75,
        "cooling": false,
        "heating": true,
        "maxCost": 13.5
      },
      "delivery": {"lng": -3.187, "lat": 55.943}
    }
  ]'
🐳 Docker Deployment
Building the Image
bash
docker build -t ilp-cw2-service .
Running the Container
bash
docker run -p 8080:8080 ilp-cw2-service
For Submission
bash
# Save Docker image to required TAR file
docker save -o ilp_submission_image.tar ilp-cw2-service

# Verify the image
docker load -i ilp_submission_image.tar
📊 Technical Specifications
ILP 2025 CW2 Compliance
✅ All CW1 endpoints maintained and functional

✅ Environment variable configuration (ILP_ENDPOINT)

✅ String-based drone IDs throughout the system

✅ Comprehensive query operations with multiple operators

✅ AND logic for multiple dispatch requirements

✅ Both cooling AND heating support when required

✅ Date/time availability checking

✅ Max cost estimation with pro-rata distribution

✅ Delivery path planning with hover points

✅ GeoJSON output for visualization

✅ Proper error handling (200 OK for most cases, 404 for invalid drone IDs)

Key CW2 Features
External Service Integration: Configurable ILP endpoint with WebClient

Advanced Query System: Support for =, !=, <, >, <=, >= operators

Availability Logic: Date/time windows, capacity, cooling/heating requirements

Path Planning: Multi-drone support, cost calculation, no-fly zone avoidance

GeoJSON Export: Standard format for path visualization

Comprehensive Testing: Unit tests for all services and integration tests for endpoints

Data Models
MedDispatchRec: Medicine dispatch records with delivery requirements

Drone: Drone entities with capabilities and specifications

ServicePoint: Drone base stations with geographic locations

RestrictedArea: No-fly zones and restricted airspace

🔧 Configuration
Environment Variables
bash
ILP_ENDPOINT=https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net
SERVER_PORT=8080
Application Properties
Default configuration in src/main/resources/application.properties:

properties
server.port=8080
spring.application.name=ilp-cw2-service
logging.level.uk.ac.ed.acp.cw2=DEBUG
🎯 Business Logic Highlights
Availability Service
AND-narrowing logic for multiple dispatches

Cooling AND heating both required when specified

Time window checking (delivery must be BEFORE end time)

Capacity and cost constraint validation

Path Planning Service
Multi-drone delivery optimization

Flight path generation with hover points

Cost calculation with pro-rata distribution

No-fly zone avoidance

Return-to-base routing

Query Service
Flexible attribute-based filtering

Support for numeric and boolean comparisons

JSON path-based attribute access

Type-safe operator handling