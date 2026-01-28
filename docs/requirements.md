# ILP CW2 Requirements Document

**Project**: Drone Delivery REST Service  
**Version**: 1.0  
**Date**: January 2026

---

## 1. FUNCTIONAL REQUIREMENTS

### 1.1 CW1 Endpoints (Re-implementation)
- **FR-1.1**: `GET /actuator/health` - Return system health status
- **FR-1.2**: `GET /api/v1/uid` - Return student ID as string
- **FR-1.3**: `POST /api/v1/distanceTo` - Calculate Euclidean distance between two positions
- **FR-1.4**: `POST /api/v1/isCloseTo` - Determine if two positions are within 0.00015 distance
- **FR-1.5**: `POST /api/v1/nextPosition` - Calculate next position given start point and angle
- **FR-1.6**: `POST /api/v1/isInRegion` - Determine if point is inside polygon region

### 1.2 Static Query Endpoints
- **FR-2.1**: `GET /api/v1/dronesWithCooling/{state}` - Return array of drone IDs with/without cooling
- **FR-2.2**: `GET /api/v1/droneDetails/{id}` - Return single drone JSON object (404 if not found)

### 1.3 Dynamic Query Endpoints
- **FR-3.1**: `GET /api/v1/queryAsPath/{attribute}/{value}` - Query drones by single attribute-value pair
- **FR-3.2**: `POST /api/v1/query` - Query drones by multiple attributes with operators (=, !=, <, >)

### 1.4 Drone Availability
- **FR-4.1**: `POST /api/v1/queryAvailableDrones` - Return drone IDs matching all dispatch requirements
- **FR-4.2**: Must consider capacity, cooling, heating, maxCost, date, and time constraints
- **FR-4.3**: Must verify drone availability by day of week

### 1.5 Delivery Path Calculation
- **FR-5.1**: `POST /api/v1/calcDeliveryPath` - Calculate optimal multi-delivery routes
    - Return structure with totalCost, totalMoves, dronePaths array
    - Each dronePath contains droneId and deliveries array
    - Each delivery contains deliveryId and flightPath (LngLat coordinates)
    - Must start and end at Drone Service Point
    - Support multiple drones from multiple service points

- **FR-5.2**: `POST /api/v1/calcDeliveryPathAsGeoJson` - Return single-drone route as valid GeoJSON
    - Must be valid GeoJSON LineString format
    - Must be viewable on https://geojson.io

### 1.6 Flight Rules Compliance
- **FR-6.1**: Only move along predefined degree angles
- **FR-6.2**: Respect no-fly zones
- **FR-6.3**: No corner cutting
- **FR-6.4**: Maintain consistent step width (0.00015 degrees)
- **FR-6.5**: Delivery indicated by two identical consecutive coordinates (hover)
- **FR-6.6**: Do not exceed drone move capacity

---

## 2. DATA VALIDATION REQUIREMENTS

### 2.1 Input Validation
- **DV-1.1**: Return 400 for syntactically incorrect JSON
- **DV-1.2**: Return 400 for semantically invalid data (CW1 endpoints)
- **DV-1.3**: Return 200 for all valid requests (except droneDetails with invalid ID)
- **DV-1.4**: Return 404 only for `droneDetails/{id}` with non-existent ID
- **DV-1.5**: Handle missing optional fields in MedDispatchRec (date, time, cooling, heating, maxCost)

### 2.2 Data Type Handling
- **DV-2.1**: Parse string values to appropriate types (numeric, boolean)
- **DV-2.2**: Handle operator strings (<, >, =, !=) for dynamic queries
- **DV-2.3**: Validate LngLat coordinate format
- **DV-2.4**: Validate LocalDate and LocalTime formats

---

## 3. PERFORMANCE REQUIREMENTS

### 3.1 Response Time
- **PR-1.1**: Simple queries (uid, health) - Target <10ms
- **PR-1.2**: Geometric calculations (distanceTo, isCloseTo) - Target <50ms
- **PR-1.3**: Static queries (dronesWithCooling, droneDetails) - Target <100ms
- **PR-1.4**: Dynamic queries - Target <200ms
- **PR-1.5**: Availability queries - Target <500ms
- **PR-1.6**: Path calculation (simple) - Target <2000ms
- **PR-1.7**: Path calculation (complex) - Target <5000ms

### 3.2 Scalability
- **PR-2.1**: Handle drone dataset of 50+ drones
- **PR-2.2**: Process multiple dispatch records (up to 20) in single request
- **PR-2.3**: Calculate paths with up to 10 deliveries in sequence

---

## 4. INTEGRATION REQUIREMENTS

### 4.1 External API Integration
- **IR-1.1**: Connect to ILP REST service at configurable endpoint
- **IR-1.2**: Default endpoint: `https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/`
- **IR-1.3**: Support environment variable `ILP_ENDPOINT` for different endpoints
- **IR-1.4**: Retrieve fresh data from external API for each endpoint call

### 4.2 Data Refresh Strategy
- **IR-2.1**: Data may change between calls - always fetch fresh data
- **IR-2.2**: No caching during endpoint processing
- **IR-2.3**: Retrieve all necessary data (drones, service points, regions) per request

---

## 5. DEPLOYMENT REQUIREMENTS

### 5.1 Docker Configuration
- **DR-1.1**: Service runs on port 8080
- **DR-1.2**: Uses Java runtime environment
- **DR-1.3**: Docker image saved as `ilp_submission_image.tar`
- **DR-1.4**: Image placed in root directory of submission

### 5.2 Build Requirements
- **DR-2.1**: Must build from submitted source code
- **DR-2.2**: All dependencies included or specified
- **DR-2.3**: No manual configuration required to run

---

## 6. QUALITY ATTRIBUTES

### 6.1 Reliability
- **QA-1.1**: Consistent results for identical inputs
- **QA-1.2**: Graceful handling of external API failures
- **QA-1.3**: No crashes on invalid data

### 6.2 Maintainability
- **QA-2.1**: Separation of concerns (Controller, Service, Repository, DTO layers)
- **QA-2.2**: Clear naming conventions
- **QA-2.3**: Comprehensive code comments
- **QA-2.4**: Modular design for easy extension

### 6.3 Testability
- **QA-3.1**: Unit testable components
- **QA-3.2**: Mockable external dependencies
- **QA-3.3**: Deterministic behavior for automated testing

---

## 7. SECURITY REQUIREMENTS

### 7.1 Input Sanitization
- **SR-1.1**: Prevent SQL injection (if database used)
- **SR-1.2**: Validate all user inputs
- **SR-1.3**: Handle malformed JSON safely

### 7.2 Error Handling
- **SR-2.1**: No stack traces exposed to end users
- **SR-2.2**: Appropriate HTTP status codes
- **SR-2.3**: Generic error messages for security-sensitive failures

---

## 8. COST CALCULATION REQUIREMENTS

### 8.1 Pro-rata Cost Distribution
- **CR-1.1**: Total flight moves × cost per move = total cost
- **CR-1.2**: Initial and final service point costs included pro-rata
- **CR-1.3**: Equal distribution across all deliveries in single drone flight
- **CR-1.4**: Example: 3 deliveries in 1,200-move flight = 400 moves per delivery cost

### 8.2 MaxCost Constraint
- **CR-2.1**: Respect maxCost in MedDispatchRec when present
- **CR-2.2**: Only allocate delivery to drone if pro-rata cost ≤ maxCost

---

## 9. ALGORITHM REQUIREMENTS

### 9.1 Path Finding
- **AR-1.1**: Find shortest valid path between points
- **AR-1.2**: Avoid no-fly zones
- **AR-1.3**: Optimize delivery sequence to minimize total moves
- **AR-1.4**: Prevent drone from running out of moves mid-flight

### 9.2 Drone Allocation
- **AR-2.1**: Match drone capabilities to delivery requirements
- **AR-2.2**: Consider availability by day of week
- **AR-2.3**: Support multi-drone solutions when single drone insufficient
- **AR-2.4**: Return empty array when no valid allocation possible

---

## 10. ACCEPTANCE CRITERIA

### 10.1 Auto-marker Success
- **AC-1.1**: Score 33/33 points on auto-marker tests
- **AC-1.2**: All endpoint names exactly match specification
- **AC-1.3**: All JSON response structures match specification

### 10.2 Manual Testing
- **AC-2.1**: All endpoints testable via Postman/curl
- **AC-2.2**: Docker image loads and runs without errors
- **AC-2.3**: Service accessible at http://localhost:8080

---

## REQUIREMENTS TRACEABILITY MATRIX

| Requirement ID | Type         | Test Level       | Priority | Test Method                   |
|---------------|--------------|------------------|----------|-------------------------------|
| FR-1.x | Functional   | Unit/Integration | High     | JUnit + API tests             |
| FR-2.x | Functional   | Integration      | High     | API tests                     |
| FR-3.x | Functional   | Integration      | High     | API tests                     |
| FR-4.x | Functional   | Integration      | Critical | API tests + Auto-marker       |
| FR-5.x | Functional   | System           | Critical | API tests + Auto-marker       |
| FR-6.x | Functional   | Unit             | High     | JUnit                         |
| DV-x.x | Data Quality | Unit/Integration | Critical | JUnit + Error injection       |
| PR-x.x | Performance  | System           | Medium   | Load testing                  |
| IR-x.x | Integration  | Integration      | High     | Mock external API             |
| DR-x.x | Deployment   | System           | Critical | Docker tests                  |
| QA-x.x | Quality      | All              | Medium   | Code review + Static analysis |
| SR-x.x | Security     | Integration      | Medium   | Security tests                |
| CR-x.x | Cost         | Unit             | High     | JUnit                         |
| AR-x.x | Functional   | Unit/Integration | Critical | Algorithm tests               |

---

## NOTES FOR TESTING

### High-Risk Areas
1. Path calculation algorithms (complex, many edge cases)
2. Cost calculation pro-rata distribution
3. Multi-attribute dynamic queries with type casting
4. Polygon region boundary detection
5. Drone availability day-of-week matching

### Test Data Needed
- Valid/invalid LngLat coordinates
- Open/closed polygon regions
- Various drone configurations (with/without cooling, different capacities)
- Multiple MedDispatchRec scenarios
- Edge cases: empty arrays, missing optional fields, boundary values

### Dependencies
- External ILP REST API availability
- Spring Boot framework
- JSON parsing library (Jackson)
- Geometric calculation utilities
