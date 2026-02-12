# ILP CW2 Requirements Document

## 1. FUNCTIONAL REQUIREMENTS

### FR-1: CW1 Geometric Utilities

**FR-1.1**: GET `/actuator/health`  
Return Spring Boot health status with `{"status": "UP"}`

**FR-1.2**: GET `/api/v1/uid`  
Return student ID as plain string: `s2581854`

**FR-1.3**: POST `/api/v1/distanceTo`  
Calculate Euclidean distance √[(x₂-x₁)²+(y₂-y₁)²] between two LngLat positions  
Accuracy: 10 decimal places  
*Test Coverage*: DroneNavigationTests.testEuclideanDistance (lines 32-80)

**FR-1.4**: POST `/api/v1/isCloseTo`  
Return true if distance between two positions < 0.00015 degrees (strictly less than)  
*Test Coverage*: DroneNavigationTests.testIsClose (lines 85-132, 5 parameterized cases)

**FR-1.5**: POST `/api/v1/nextPosition`  
Calculate next position given start LngLat and angle from 16 allowed compass directions  
Step size: 0.00015 degrees (constant across all directions)  
*Test Coverage*: DroneNavigationTests.testNextPosition (lines 149-267, parameterized 16 directions and other edge cases)

**FR-1.6**: POST `/api/v1/isInRegion`  
Determine if point is inside named polygon region using ray-casting algorithm  
*Test Coverage*: PointInRegionTests (16 test cases lines 31-254)

---

### FR-2: Static Drone Queries

**FR-2.1**: GET `/api/v1/dronesWithCooling/{state}`  
Return array of drone IDs (as STRING) with cooling capability matching boolean state  
*Test Coverage*: ControllerTests.testDronesWithCooling (lines 127-146)

**FR-2.2**: GET `/api/v1/droneDetails/{id}`  
Return single drone JSON object for given STRING ID  
HTTP 404 if ID not found  
*Test Coverage*: ControllerTests.testDroneDetails (lines 149-174)

---

### FR-3: Dynamic Drone Queries

**FR-3.1**: GET `/api/v1/queryAsPath/{attribute}/{value}`  
Query drones by single attribute-value pair  
Return array of drone IDs (STRING)  
*Test Coverage*: ControllerTests.testQueryAsPath (line 177)

**FR-3.2**: POST `/api/v1/query`  
Query drones by multiple attributes with operators (=, !=, <, >, <=, >=)  
AND logic: all conditions must match  
Return array of drone IDs (STRING)  
*Test Coverage*: ControllerTests.testQuery (line 214), DroneQueryTests (20 tests lines 117-435)

---

### FR-4: Drone Availability Matching

**FR-4.1**: POST `/api/v1/queryAvailableDrones`  
Match drones to delivery requirements using AND logic:
- Capacity: drone.capacity ≥ requirement.capacity
- Cooling: if requirement.cooling=true, drone must have cooling
- Heating: if requirement.heating=true, drone must have heating
- Day/Time: delivery date/time must fall within drone availability schedule
- MaxCost: pro-rata cost per delivery ≤ requirement.maxCost (if specified)

Return array of drone IDs (STRING) that satisfy ALL requirements  
*Test Coverage*: AvailabilityTests (18 tests lines 157-408)

---

### FR-5: Delivery Path Planning

**FR-5.1**: POST `/api/v1/calcDeliveryPath`  
Calculate optimal delivery routes returning JSON structure:
```json
{
  "totalCost": <double>,
  "totalMoves": <integer>,
  "dronePaths": [
    {
      "droneId": "<STRING>",
      "deliveries": [
        {
          "deliveryId": <integer>,
          "flightPath": [{"lng": <double>, "lat": <double>}, ...]
        }
      ]
    }
  ]
}
```
*Test Coverage*: PathPlanningTests (12 tests: lines 117-382)

**FR-5.2**: POST `/api/v1/calcDeliveryPathAsGeoJson`  
Return single-drone route as valid GeoJSON LineString format  
Must be viewable on https://geojson.io  
*Test Coverage*: PathPlanningTests.testCalcDeliveryPathAsGeoJson (lines 326-360)

---

### FR-6: Flight Rules Compliance

**FR-6.1**: Allowed Angles  
Only 16 compass directions: 0°, 22.5°, 45°, 67.5°, 90°, 112.5°, 135°, 157.5°, 180°, 202.5°, 225°, 247.5°, 270°, 292.5°, 315°, 337.5°  
*Test Coverage*: DroneNavigationTests parameterized (lines 149-190)

**FR-6.2**: No-Fly Zone Avoidance  
Flight path must never enter restricted areas (altitude limits: upper=-1, lower=0)  
*Test Coverage*: PointInRegionTests (17 scenarios including concave shapes, boundaries lines 31-228)

**FR-6.3**: Step Width Consistency  
All moves must be exactly 0.00015 degrees regardless of direction  
*Test Coverage*: DroneNavigationTests.testSTEP_CorrectValue (line 272)

**FR-6.4**: Delivery Hover Indication  
Delivery point must be indicated by two consecutive identical LngLat coordinates  
*Test Coverage*: PathPlanningTests.testCalcDeliveryPath_HoverRepresentedAsTwoIdenticalCoordinates (lines 169-200)

**FR-6.5**: Service Point Return  
Flight path must start and end at same drone service point  
*Test Coverage*: for example, PathPlanningTests.testCalcDeliveryPath_FlightPathStartsAtServicePoint (lines 143-166)

---

## 2. DATA VALIDATION REQUIREMENTS

### DV-1: Input Validation

**DV-1.1**: HTTP 400 for syntactically invalid JSON  
Missing quotes, trailing commas, unclosed brackets  
*Test Coverage*: ControllerTests error injection tests

**DV-1.2**: HTTP 400 for semantically invalid data  
Null coordinate values, negative capacity, out-of-bounds coordinates  
*Test Coverage*: ControllerTests.testInvalidInput (line 129)

**DV-1.3**: HTTP 404 for non-existent drone ID  
Only applies to `/api/v1/droneDetails/{id}` endpoint  
*Test Coverage*: ControllerTests.testDroneDetailsInvalidId (line 169)

**DV-1.4**: Unclosed Polygon Rejection  
Region where last vertex ≠ first vertex must be rejected  
*Test Coverage*: PointInRegionTests.testisPointInRegion_UnclosedPolygon_ReturnsFalse (line 202)

---

## 3. PERFORMANCE REQUIREMENTS

### PR-1: Simple Query Response Time

Target: <100ms for geometric calculations and simple queries  
**Measured**:
- distanceTo: ~450ms average
- isCloseTo: ~450ms average
- droneDetails: ~450ms average

*Test Coverage*: Informal timing via test execution logs

### PR-2: Complex Operation Response Time

Target: <5s for path calculation  
**Measured**: ~487ms for 2-delivery scenario  
Complex scenarios (5+ drones, 10+ deliveries) not tested due to algorithmic complexity

*Test Coverage*: PathPlanningTests execution timing

### PR-3: Container Startup

Target: <50s from docker run to ready state  
**Measured**: ~26s (docker logs timestamp analysis)

---

## 4. QUALITY ATTRIBUTES

### QA-1: Maintainability

**QA-1.1**: Layered Architecture  
Controllers (REST contract), Services (business logic), DTOs (data transfer), Models (domain), Configuration (environment)  
*Test Coverage*: ApplicationTests bean initialization (lines 3-10)

**QA-1.2**: Separation of Concerns  
Controllers contain zero business logic, only delegation  
*Test Coverage*: Architecture verified via code review

**QA-1.3**: Dependency Injection  
@Autowired services enable testability via mocking  
*Test Coverage*: AvailabilityTests @Mock pattern (line 10)

---

### QA-2: Reliability

**QA-2.1**: Consistent Error Handling  
HTTP 200 for success, 400 for client validation errors, 404 for not found  
*Test Coverage*: ControllerTests validates all status codes (lines 56-389)

**QA-2.2**: Graceful Degradation  
Service handles null inputs, empty lists without crashes  
*Test Coverage*: AvailabilityTests.testQueryAvailableDrones_EmptyList_ReturnsEmpty (lines 344)

---

### QA-3: Testability

**QA-3.1**: Mock-Friendly Design  
External dependencies injectable and mockable  
*Test Coverage*: All service tests use @Mock pattern (AvailabilityTests line 10, DroneQueryTests line 10, PathPlanningTests line 7)

**QA-3.2**: Deterministic Behavior  
No random elements, time-based logic uses injected LocalDate  
*Test Coverage*: 100% reproducibility across 60+ test runs

---

## 5. SAFETY-CRITICAL REQUIREMENTS

### R1: No-Fly Zone Compliance

**Priority**: CRITICAL

**Specification**: Drones must NEVER enter restricted airspace  
- Polygon membership via ray-casting algorithm
- Includes boundary vertices and edges
- Handles concave polygons correctly

**Testing Strategy**: Dual verification
1. Unit testing: PointInRegionTests 18 comprehensive scenarios (95% coverage)
   - Points inside rectangular regions (line 31)
   - Points outside (line 41)
   - Points on vertices (line 51)
   - Points on edges (line 61)
   - Concave L-shaped polygons (lines 95-132)
   - Triangular regions (lines 73-90)
   - Edinburgh operational bounds (lines 137-168)

2. Integration testing: PathPlanningTests verifies no path coordinates fall within restricted areas

**Acceptance Criteria**: 100% of flight paths avoid all restricted areas

---

### R3: Drone Allocation AND Logic

**Priority**: HIGH

**Specification**: Drone matches delivery ONLY if ALL requirements satisfied:
- Capacity ≥ requirement
- Cooling matches (if specified)
- Heating matches (if specified)
- Available on delivery day
- Available at delivery time
- Cost within maxCost budget (if specified)

**Testing Strategy**: Explicit negative testing
- AvailabilityTests.testQueryAvailableDrones_SimpleDispatch_ReturnsMultipleDrones (line 157): Only drones matching ALL constraints returned
- AvailabilityTests.testQueryAvailableDrones_BothCoolingAndHeating_RequiresBoth (line 213): Both cooling AND heating required
- AvailabilityTests.testQueryAvailableDrones_NoMatchingDrone (line 353): Empty result when no drone satisfies all

**Acceptance Criteria**: Empty array returned if ANY requirement unmet

---

## 6. REQUIREMENT TRACEABILITY MATRIX

| Requirement | Test Class | Coverage |
|-------------|-----------|-------------------|----------|
| FR-1.3 Distance | DroneNavigationTests | 90% |
| FR-1.4 Proximity | DroneNavigationTests | 90% |
| FR-1.5 Next Position | DroneNavigationTests | 90% |
| FR-1.6 Point in Region | PointInRegionTests | 95% |
| FR-2.1 Cooling Query | ControllerTests | 79% |
| FR-2.2 Drone Details | ControllerTests | 79% |
| FR-3.1 Query as Path | ControllerTests | 79% |
| FR-3.2 Query POST | DroneQueryTests | 85% |
| FR-4.1 Availability | AvailabilityTests | 84% |
| FR-5.1 Path Calculation | PathPlanningTests | 81% |
| FR-6.2 No-Fly Zones | PointInRegionTests | 95% |
| R1 Safety | PointInRegionTests + PathPlanningTests | 95%/81% |
| R3 AND Logic | AvailabilityTests | 84% |

---

## 7. KNOWN LIMITATIONS

### Untested Scenarios

1. **Complex Multi-Drone**: 5+ drones, 10+ deliveries, multiple service points
   - Reason: Combinatorial explosion, algorithmic complexity
   - Risk: Medium
   - Mitigation: Simple scenarios (1-2 deliveries) thoroughly tested

2. **Concurrency**: Multi-threaded request handling
   - Reason: No JMeter load testing infrastructure
   - Risk: Medium
   - Mitigation: Service likely stateless

3. **Temporal Exhaustiveness**: Leap years, timezone transitions
   - Reason: Requirements don't specify timezone handling
   - Risk: Low
   - Mitigation: Day-of-week logic tested with representative dates

4. **External API Resilience**: Timeout/failure scenarios
   - Reason: Limited WireMock fault injection
   - Risk: Medium
   - Mitigation: Mock testing covers happy paths

5. **Security**: Penetration testing, OWASP validation
   - Reason: Out of scope for coursework
   - Risk: Low coursework, High production
   - Mitigation: Spring Boot defaults, @Valid annotations

---

## 8. REQUIREMENTS SIGN-OFF

**Test Coverage Target**: 75% overall instruction, 80% service layer  
**Achieved**: 76% overall, 82% service layer  
**Status**: TARGET EXCEEDED

**Endpoint Coverage Target**: 100% all endpoints tested  
**Achieved**: 100% (11/11 endpoints)  
**Status**: TARGET MET

**Critical Path Coverage**: PointInRegion 95%, AvailabilityService 84%  
**Status**: CRITICAL PATHS VALIDATED
