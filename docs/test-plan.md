# Test Plan - ILP CW2 Drone Delivery Service

## Requirements Overview

The ILP CW2 system has multiple requirement levels with varying priorities and A&T (Analysis & Testing) needs:

### High Priority Requirements (Safety-Critical)

**R1 (System-Level Safety)**: No drone shall enter a no-fly zone during any delivery flight path
- **Priority**: Critical - could result in regulatory violations, physical damage, or legal liability
- **Type**: Safety property requiring verification
- **A&T Strategy**: Multiple independent verification methods as per Chapter 3 principles

**R2 (Data Integrity)**: Service must return HTTP 400 for all syntactically or semantically invalid inputs
- **Priority**: High - prevents cascading failures and undefined behavior
- **Type**: Robustness requirement
- **A&T Strategy**: Exhaustive error injection testing

**R3 (Functional Correctness)**: Drone allocation must match ALL requirements (capacity, cooling, heating, maxCost, day availability) via AND logic
- **Priority**: Critical - incorrect allocation could result in failed deliveries
- **Type**: Business logic correctness
- **A&T Strategy**: Equivalence partitioning with boundary analysis

### Medium Priority Requirements

**R4 (Performance)**: Simple queries (uid, droneDetails) respond within 100ms; complex path calculations within 5 seconds
- **Priority**: Medium - affects user experience but not safety
- **Type**: Measurable quality attribute
- **A&T Strategy**: Statistical performance testing with synthetic and real data

**R5 (Integration)**: Service must retrieve fresh data from external ILP endpoint on every request (no stale caching)
- **Priority**: Medium - ensures data consistency
- **Type**: Integration requirement
- **A&T Strategy**: Mock-based verification with timestamp validation

### Lower Priority Requirements

**R6 (Deployment)**: Docker image must build from sources and run on port 8080 within 30 seconds
- **Priority**: Lower - convenience requirement
- **Type**: Operational requirement
- **A&T Strategy**: Simple acceptance test in CI/CD

---

## Priority Assessment and Resource Allocation

Following Chapter 3 engineering principles:

### R1 (No-Fly Zone Safety) - Resource Allocation: 25%
- **Partition Principle**: Decompose into two components:
    1. **PointInRegion utility** - Pure geometric algorithm verifying point-in-polygon (interlocking)
    2. **DroneNavigation service** - Path planning that consults PointInRegion before each move
- **Early Detection (Chapter 4)**: Implement unit tests for PointInRegion BEFORE integration testing
- **Multiple Approaches**:
    - Formal inspection of ray-casting algorithm implementation
    - Exhaustive unit testing of geometric edge cases
    - Integration testing with realistic restricted area data
    - System-level validation with complete flight paths

### R2 (Input Validation) - Resource Allocation: 15%
- **Principle**: Error cases are predictable and testable
- **Approach**: Systematic error injection for each endpoint
- **Early Detection**: Test-driven development with error cases written first

### R3 (Drone Allocation Logic) - Resource Allocation: 20%
- **Partition Principle**: Separate filtering logic by requirement type (capacity, capabilities, availability)
- **Approach**: Equivalence partitioning for each filter, combination testing for AND logic

### R4 (Performance) - Resource Allocation: 15%
- **Later Validation**: System-level testing near completion
- **Risk**: Synthetic data may not represent real-world load distribution

### R5 (Integration) - Resource Allocation: 15%
- **Approach**: Mock-based testing for development, smoke tests against real endpoint
- **Risk**: External API availability during testing

### R6 (Deployment) - Resource Allocation: 10%
- **Approach**: Automated CI/CD acceptance test

---

## Test Levels and Approaches

### Unit Testing (30% of total tests)

**Components to Unit Test**:

1. **PointInRegion (Safety-Critical R1)**
    - Inputs: LngLat point, Region polygon
    - Outputs: boolean (inside/outside)
    - Specification: Ray-casting algorithm - cast ray from point, count edge intersections, odd=inside
    - **Tasks**:
        - T1.1: Inspect code against ray-casting specification (no extraneous logic)
        - T1.2: Test point clearly inside rectangular region
        - T1.3: Test point clearly outside region
        - T1.4: Boundary cases: point exactly on edge, on vertex
        - T1.5: Degenerate cases: horizontal/vertical edges, collinear points
        - T1.6: Complex polygons: concave shapes, many vertices
        - T1.7: Invalid input: unclosed region (expect validation error)
        - T1.8: Exhaustive test with randomly generated points and known regions (1000+ iterations)

2. **Geometric Calculations (R3 prerequisites)**
    - distanceTo: Euclidean distance formula √[(x₂-x₁)² + (y₂-y₁)²]
    - isCloseTo: Distance < 0.00015 threshold
    - nextPosition: Move 0.00015 in specified compass direction
    - **Tasks**:
        - T2.1: Test with known coordinate pairs (manual calculation verification)
        - T2.2: Boundary: exactly at 0.00015 threshold
        - T2.3: Test all 16 compass directions (0°, 22.5°, 45°, ..., 337.5°)
        - T2.4: Test edge cases: same point (distance=0), antipodal points

3. **Query Logic (R3)**
    - Attribute filtering with type casting (string→numeric, string→boolean)
    - Operator evaluation (=, !=, <, >)
    - **Tasks**:
        - T3.1: Test string equality for text attributes
        - T3.2: Test numeric comparisons with type casting
        - T3.3: Test boolean filtering
        - T3.4: Test invalid type casts (expect error handling)

### Integration Testing (40% of total tests)

**Integration Scenarios**:

1. **Availability Service Integration (R3, R5)**
    - Combines: QueryService + IlpClientService + date/time validation
    - **Tasks**:
        - T4.1: Mock IlpClientService to return known drone set
        - T4.2: Test capacity filtering (drones with capacity ≥ required)
        - T4.3: Test cooling/heating filtering (either/or logic)
        - T4.4: Test day-of-week availability matching
        - T4.5: Test maxCost filtering with pro-rata calculation
        - T4.6: Test AND logic: all requirements must match
        - T4.7: Test empty result when no drones match
        - T4.8: Verify fresh data fetch (mock called on each request)

2. **Path Planning Integration (R1, R3)**
    - Combines: DroneNavigation + PointInRegion + IlpClientService
    - **Tasks**:
        - T5.1: Simple path: 1 delivery, no obstacles, straight line
        - T5.2: Path avoiding single no-fly zone
        - T5.3: Path with delivery hover (consecutive identical coordinates)
        - T5.4: Multi-delivery sequence (2-3 deliveries)
        - T5.5: Return to service point verification
        - T5.6: Move count within drone capacity
        - T5.7: Cost calculation accuracy (pro-rata distribution)

3. **External API Integration (R5)**
    - **Scaffolding**: MockRestServiceServer to simulate ILP endpoint
    - **Tasks**:
        - T6.1: Test successful data retrieval
        - T6.2: Test timeout handling
        - T6.3: Test malformed JSON response
        - T6.4: Test 500 error response
        - T6.5: Verify environment variable configuration (ILP_ENDPOINT)

### System Testing (30% of total tests)

**REST Endpoint Testing via MockMvc**:

1. **CW1 Endpoints (R2)**
    - **Tasks**:
        - T7.1: Valid requests return 200 + correct data
        - T7.2: Invalid JSON syntax returns 400
        - T7.3: Semantically invalid data returns 400 (null values, out-of-range)
        - T7.4: Extra JSON fields ignored gracefully

2. **Query Endpoints (R2, R3)**
    - **Tasks**:
        - T8.1: dronesWithCooling returns correct drone IDs
        - T8.2: droneDetails with valid ID returns 200 + drone object
        - T8.3: droneDetails with invalid ID returns 404
        - T8.4: queryAsPath with valid attribute returns filtered results
        - T8.5: query POST with multiple criteria returns correct AND results

3. **Delivery Path Endpoints (R1, R3, R4)**
    - **Tasks**:
        - T9.1: calcDeliveryPath simple case (3 points auto-marker)
        - T9.2: calcDeliveryPath complex case (7 points auto-marker)
        - T9.3: calcDeliveryPathAsGeoJson produces valid GeoJSON
        - T9.4: GeoJSON viewable on https://geojson.io (manual validation)
        - T9.5: Performance: simple case completes <2s
        - T9.6: Performance: complex case completes <5s

---

## Scaffolding and Instrumentation

### Scaffolding Required

1. **Mock External API (for R5 testing)**
    - **Task S1**: Create MockRestServiceServer configurations
    - **Effort**: 2 hours
    - **Dependency**: None
    - **Deliverable**: Test configuration class with predefined drone/service point responses

2. **Test Data Generator (for R4 performance testing)**
    - **Task S2**: Generate synthetic MedDispatchRec datasets
    - **Effort**: 3 hours
    - **Dependency**: None
    - **Deliverable**: Utility class producing varied delivery scenarios

3. **GeoJSON Validator (for R1, system tests)**
    - **Task S3**: Automated check for valid GeoJSON structure
    - **Effort**: 1 hour
    - **Dependency**: None
    - **Deliverable**: Assertion helper method

### Instrumentation Required

1. **Debug Logging (for R1 path verification)**
    - **Task I1**: Add DEBUG logs in DroneNavigation showing:
        - Each move calculation
        - No-fly zone checks
        - Move counter updates
    - **Effort**: 1 hour
    - **Deliverable**: Enhanced logging in navigation service

2. **Performance Metrics (for R4)**
    - **Task I2**: Add execution time logging for:
        - Query operations
        - Path calculations
        - External API calls
    - **Effort**: 2 hours
    - **Dependency**: Logging framework setup
    - **Deliverable**: Metrics output in test reports

3. **Request/Response Logging (for R2, R5)**
    - **Task I3**: Log all incoming requests and responses
    - **Effort**: 1 hour
    - **Deliverable**: Audit trail for debugging

---

## Process and Lifecycle Integration

### Phase 1: Foundation (Week 1)
**TDD Approach - Unit Tests First**

- **S1**: Setup mock external API scaffolding (2h)
- **S2**: Create test data generator (3h)
- **T1.1-T1.8**: Implement PointInRegion tests (4h)
    - Write tests FIRST
    - Implement ray-casting algorithm to pass tests
    - **Risk Mitigation**: Formal inspection of algorithm against specification
- **T2.1-T2.4**: Implement geometric calculation tests (3h)
    - Write tests for all 16 compass directions
    - Implement nextPosition algorithm
- **I1**: Add debug logging to navigation (1h)
- **Deliverable**: Core geometric utilities with 100% unit test coverage

### Phase 2: Integration (Week 2)
**Building on Tested Components**

- **T3.1-T3.4**: Query logic tests (3h)
- **T4.1-T4.8**: Availability service integration tests (6h)
    - Test each filter independently, then combined
    - Verify AND logic correctness
- **T6.1-T6.5**: External API integration tests (4h)
    - Use scaffolding from S1
- **I2, I3**: Add performance and request logging (3h)
- **Deliverable**: Integrated services with mocked dependencies

### Phase 3: System Validation (Week 3)
**End-to-End Testing**

- **T7.1-T7.4**: CW1 endpoint tests (3h)
- **T8.1-T8.5**: Query endpoint tests (4h)
- **T5.1-T5.7**: Path planning integration tests (8h)
    - Start simple, build to complex
    - **Risk**: Complex multi-drone scenarios may reveal algorithmic issues
- **T9.1-T9.4**: Delivery path system tests (6h)
- **S3**: GeoJSON validator (1h)
- **Deliverable**: Complete REST API tested against auto-marker scenarios

### Phase 4: Performance and Deployment (Week 4)
**Non-Functional Testing**

- **T9.5-T9.6**: Performance validation (4h)
    - Run with synthetic data from S2
    - Measure response times
    - **Risk**: Synthetic data may not match real load patterns
    - **Mitigation**: If possible, collect sample real data and feed back into testing
- **T10**: Docker deployment test (2h)
    - Build image, test startup time
    - Verify port 8080 accessibility
- **CI/CD Setup**: GitHub Actions automation (3h)
- **Deliverable**: Production-ready service with automated testing

---

## Risk Assessment and Mitigation

### High-Impact Risks

**R1 Safety Risk**: Path planning fails to detect no-fly zone collision
- **Likelihood**: Low (extensive testing planned)
- **Impact**: Critical
- **Mitigation**:
    - Two-stage verification (PointInRegion unit tests + integration tests)
    - Formal code inspection against specification
    - Exhaustive random testing (1000+ test cases)
    - Manual validation of generated GeoJSON paths

**R3 Logic Risk**: AND logic incorrectly filters drones (OR behavior)
- **Likelihood**: Medium (common logic error)
- **Impact**: High (wrong drone allocation)
- **Mitigation**:
    - Explicit test cases for each filter independently
    - Test cases requiring ALL filters to match
    - Test cases where only SOME filters match (expect empty result)

### Medium-Impact Risks

**R4 Performance Risk**: Synthetic test data doesn't represent real query patterns
- **Likelihood**: High
- **Impact**: Medium
- **Mitigation**:
    - Start with simple scenarios known from spec
    - If real data available, incorporate into later testing
    - Monitor actual production performance post-deployment
    - Performance tests positioned late to allow feedback incorporation

**R5 Integration Risk**: External ILP API unavailable during testing
- **Likelihood**: Medium
- **Impact**: Medium
- **Mitigation**:
    - All development/testing uses mocked API
    - Smoke tests against real API scheduled separately
    - Environment variable allows endpoint switching
    - Degrade gracefully if external API fails

### Lower-Impact Risks

**Test Maintenance Risk**: Spec changes require test updates
- **Likelihood**: Low (spec is fixed)
- **Impact**: Low (tests are version controlled)
- **Mitigation**: Clear requirement traceability in test names

---

## Test Coverage Goals and Evaluation Criteria

### Coverage Targets

- **Statement Coverage**: ≥85% (measured via IDE coverage tools or JaCoCo)
- **Endpoint Coverage**: 100% (all 11+ REST endpoints)
- **Requirement Coverage**: 100% (each R1-R6 has mapped test cases)
- **Error Path Coverage**: ≥70% (all 400/404 scenarios tested)

### Pass Criteria

- **Unit Tests**: 100% pass (blocking - cannot proceed to integration)
- **Integration Tests**: 100% pass
- **System Tests**: ≥95% pass (complex scenarios may be disabled if incomplete)
- **Performance Tests**: Meet R4 targets for tested scenarios
- **Auto-Marker**: 33/33 points (ultimate validation)

### Evaluation Methods

1. **Code Coverage Analysis**: JaCoCo reports in CI/CD
2. **Auto-Marker Validation**: Final submission test
3. **Manual Inspection**: Code review against specifications (R1 safety-critical)
4. **Performance Profiling**: Execution time logging analysis
5. **GeoJSON Visual Validation**: Manual check on geojson.io

---

## Test Data Strategy

### Valid Test Data
- Edinburgh coordinates: lng ∈ [-3.2, -3.15], lat ∈ [55.93, 55.96]
- Drone capacities: 0.5, 1.0, 2.0, 5.0, 10.0
- Dates: Weekdays and weekends covering all days-of-week
- Times: Morning (08:00), afternoon (14:00), evening (18:00)

### Invalid Test Data (for R2)
- Null coordinate values
- Out-of-range coordinates
- Malformed JSON (missing quotes, trailing commas)
- Wrong data types (string for numeric field)
- Unclosed polygon regions
- Negative capacities

### Edge Cases
- Distance exactly 0.00015 (isCloseTo boundary)
- Polygon points on horizontal/vertical lines
- Drone capacity exactly matching requirement
- Delivery cost exactly at maxCost limit
- No available drones (empty result scenario)

---

## Summary of Scheduled Tasks

| Task ID | Description | Effort | Phase | Dependency | Priority |
|---------|-------------|--------|-------|------------|----------|
| S1 | Mock API scaffolding | 2h | 1 | - | High |
| S2 | Test data generator | 3h | 1 | - | Medium |
| S3 | GeoJSON validator | 1h | 3 | - | Low |
| T1.1-1.8 | PointInRegion tests | 4h | 1 | - | Critical |
| T2.1-2.4 | Geometric calc tests | 3h | 1 | - | High |
| T3.1-3.4 | Query logic tests | 3h | 2 | - | High |
| T4.1-4.8 | Availability integration | 6h | 2 | S1 | Critical |
| T5.1-5.7 | Path planning integration | 8h | 3 | T1, T2 | Critical |
| T6.1-6.5 | External API integration | 4h | 2 | S1 | High |
| T7.1-7.4 | CW1 endpoint tests | 3h | 3 | - | High |
| T8.1-8.5 | Query endpoint tests | 4h | 3 | T3, T4 | High |
| T9.1-9.6 | Path endpoint tests | 6h | 3 | T5 | Critical |
| I1-I3 | Instrumentation | 4h | 2 | - | Medium |
| T10 | Docker test | 2h | 4 | All | Medium |
| CI/CD | Automation setup | 3h | 4 | All | Medium |

**Total Estimated Effort**: 56 hours (within 50-hour budget allowing for iterations)
