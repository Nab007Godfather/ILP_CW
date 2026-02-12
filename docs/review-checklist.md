# Code Review Checklist - ILP CW2 Drone Delivery Service

## 1. Naming Conventions

### Classes 
- [x] **PascalCase applied consistently**
    - Examples: `DroneNavigation`, `PathPlanningService`, `IlpClientService`
    - No violations found

### Methods 
- [x] **camelCase applied consistently**
    - Examples: `calculateDistance()`, `isInRegion()`, `queryAvailableDrones()`
    - Descriptive action verbs used
    - No single-letter or cryptic names

### Variables
- [x] **Meaningful, descriptive names**
    - Good: `availableDrones`, `flightPath`, `totalMoves`, `restrictedAreas`
    - Avoid: `d`, `temp`, `x` (except in mathematical contexts where appropriate)
    - Loop variables appropriately named: `drone`, `delivery`, `point`

### Constants 
- [~] **UPPER_SNAKE_CASE where applicable**
    - Found: `STEP_SIZE = 0.00015` in DroneNavigation ✓
    - Issue: Some magic numbers not extracted to constants
    - Example: `16` (compass directions) could be `NUM_COMPASS_DIRECTIONS`

**Rating**: 4/5 - Excellent overall and minor improvements are possible to be implemented.

---

## 2. Code Structure and Architecture

### Separation of Concerns 
- [x] **Controller layer** - REST endpoints only, no business logic
    - `CoreRestController`: CW1 endpoints
    - `QueryController`: Drone query endpoints
    - `DroneDeliveryController`: Path planning endpoints
    - **Assessment**: Clean separation and controllers delegate to services

- [x] **Service layer** - Business logic isolated
    - `AvailabilityService`: Drone filtering logic
    - `PathPlanningService`: Route calculation
    - `IlpClientService`: External API integration
    - `QueryService`: Dynamic query handling
    - **Assessment**: Single Responsibility Principle followed

- [x] **DTO layer** - Data transfer objects separate from domain
    - Request DTOs: `DistanceRequest`, `MedDispatchRec`, `QueryAttribute`
    - Response DTOs: `DeliveryPathResponse`, `DronePath`, `Delivery`
    - **Assessment**: Clean API contract abd no coupling to external models is happening

- [x] **Model layer** - Domain objects for external data
    - `Drone`, `ServicePoint`, `Region`, `RestrictedArea`
    - **Assessment**: Appropriate for external API deserialization

### Single Responsibility Principle 
- [x] Each class has clear and focused purpose
- [x] Methods do one specific thing well

### Dependency Injection ✓
- [x] `@Autowired` used appropriately for services
- [x] Constructor injection preferred over field injection
- [x] Configuration via `@Configuration` class

**Rating**: 5/5 - Excellent architectural separation

---

## 3. Code Quality Issues

### High Priority Issues

#### Issue #1: Cyclomatic Complexity in AvailabilityService
**Location**: `AvailabilityService.java`, method `isDroneAvailable()` (lines 172-199)  
**Severity**: Medium  
**Description**: Nested loops for delivery sequencing result in complexity >130%
**Code Snippet**:
    ```java
for (DroneForServicePoint dfsp : dronesForServicePoints) {
            for (DroneForServicePoint.DroneAvailability da : dfsp.getDrones()) {
                if (!da.getId().equals(droneId)) {
                    continue;
                }
                ....
    ```
**Impact**: Difficult to test all paths, harder to maintain  
**Recommendation**: Extract getDrones() as a helper method 
**Priority**: Medium (works correctly, but maintainability concern)

#### Issue #2: Type Casting Strategy in QueryService
**Location**: `QueryService.java`, method `matchesSingleAttribute()` (lines 106-142)  
**Severity**: Low  
**Description**: Long if-else chain for type casting  
**Code Snippet**:
    ```java
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
    ```
**Impact**: Maintainability - adding new types requires modification  
**Recommendation**: Use Strategy pattern or visitor pattern for type-specific operations  
**Priority**: Low (functional, enhancement for future)

### Medium Priority Issues

#### Issue #3: Magic Numbers
**Location**: Various files  
**Severity**: Low  
**Examples**:
- `0.00015` appears in multiple places (should be constant)
- `999` as hover angle (DroneNavigation.java line 42)
- `16` for compass directions

**Recommendation**: Extract to named constants at class level  
**Priority**: Low (clear from context, but best practice)

#### Issue #4: Potential Null Pointer in Optional Handling
**Location**: `AvailabilityService.java`, line 219 for example
**Severity**: Medium  
**Description**: Optional fields in methods accessed without null checks in some paths  
**Mitigation**: Currently works because validation happens earlier, but fragile  
**Recommendation**: Add explicit `Objects.requireNonNull()` or Optional wrapping  
**Priority**: Medium (defensive programming)

### Positive Findings 

#### Strength #1: Excellent Use of Java Streams
**Location**: `QueryService.java` 
**Example**:
    ```java
    ...
    return allDrones.stream()
                .filter(drone -> drone.getCapability() != null)
                .filter(drone -> drone.getCapability().getCooling() != null)
                .filter(drone -> drone.getCapability().getCooling() == hasCooling)
                .map(Drone::getId)
                .collect(Collectors.toList());
    ... 
    ```
**Assessment**: Clean, readable, functional style. No loops where streams more appropriate.

#### Strength #2: Clear Exception Handling
**Location**: `RestExceptionHandler.java`  
**Assessment**: Global exception handling via `@RestControllerAdvice` centralizes error responses

#### Strength #3: Consistent Error Responses
**Location**: All controllers  
**Assessment**: Proper HTTP status codes (200, 400, 404) used consistently

---

## 4. Error Handling and Validation

### Input Validation 
- [x] **Controller level validation**
    - `@Valid` annotations used on request bodies
    - Null checks for required fields
    - Type validation via Spring deserialization

### HTTP Status Codes 
- [x] **200 OK**: Valid requests
- [x] **400 Bad Request**: Invalid input (malformed JSON, null values)
- [x] **404 Not Found**: Resource not found (droneDetails with invalid ID)
- [x] **Consistent usage** across all endpoints

### Exception Handling 
- [~] **Global exception handler defined** but not fully exercised
    - `RestExceptionHandler` has methods for various exception types
    - Low test coverage (4%) indicates some handlers may be unused
    - **Recommendation**: Verify all exception paths work as expected

### Logging
- [x] **INFO level** for external API calls
- [x] **DEBUG level** for algorithm decisions
- [~] **Missing**: ERROR level for exception cases
- [~] **Missing**: Request/response logging for audit trail

**Rating**: 3/5 - Good validation, some gaps in exception coverage

---

## 5. Performance and Efficiency

### Algorithmic Efficiency 
- [x] **Point-in-polygon**: Ray-casting algorithm O(n) where n = vertices
- [x] **Drone filtering**: Streams with short-circuit evaluation
- [x] **Path planning**: Greedy nearest-neighbor (not optimal, but reasonable)

### Potential Bottlenecks 
- [~] **PathPlanningService**: Nested loops could be O(n³) for large inputs
    - Mitigation: Current drone/delivery counts are small (<20)
    - **Future**: Consider optimization if scaling required

### Resource Management 
- [x] **No resource leaks**: RestTemplate managed by Spring
- [x] **No unnecessary object creation** in hot paths
- [~] **Potential improvement**: Cache external API results (currently fetches every request)
    - Design choice: Spec requires fresh data, so caching would be incorrect

**Rating**: 4/5 - Efficient for current scale, aware of future bottlenecks

---

## 6. Testing and Testability

### Test Coverage (from JaCoCo)
- **Overall**: 76% 
- **Services**: 82%
- **Controllers**: 79% 
- **DTOs**: 89% 
- **Models**: 66% 
- **Exceptions**: 4% 

### Testability Features 
- [x] **Dependency injection** enables mocking
- [x] **Pure functions** where possible (geometric calculations)
- [x] **No static methods** (except utility classes)
- [x] **Clear interfaces** between layers

### Test Quality
- [x] **Unit tests** test individual components
- [x] **Integration tests** test service interactions
- [x] **MockMvc** for controller testing (no need for full server)
- [x] **Parameterized tests** reduce duplication (DroneNavigationTests)

**Rating**: 4/5 - Good testability, high coverage in critical areas

---

## 7. Security Considerations

### Input Sanitization 
- [x] **Spring validation** prevents type confusion
- [x] **No SQL injection risk** (no database)
- [x] **JSON deserialization** uses safe Jackson configuration

### Error Information Disclosure 
- [~] **Stack traces**: Currently not exposed (RestExceptionHandler prevents)
- [~] **Error messages**: Generic enough (no internal paths revealed)
- [x] **No sensitive data in logs**

### Configuration Security 
- [x] **Environment variables** for configuration (ILP_ENDPOINT)
- [x] **No hardcoded credentials**

**Rating**: 4/5 - Good security practices, no obvious vulnerabilities

---

## 8. Documentation and Comments

### Code Comments
- [~] **Javadoc**: Some classes have it, others don't
    - Present: `DayAvailability` (lines 8-11)
    - Missing: Most service methods
    - **Recommendation**: Add Javadoc for public API methods

### Inline Comments 
- [~] **Complex logic explained**: PathPlanningService has good comments especially in its most complex methods
- [~] **Sparse elsewhere**: Could improve in QueryService and AvailabilityService
- [x] **No commented-out code** ✓

### README and Documentation
- [x] **README.md** present with build instructions
- [x] **sample-requests.http** provides API examples
- [~] **API documentation**: Could benefit from Swagger/OpenAPI spec

**Rating**: 3/5 - Adequate for development, could improve for handover

---

## 9. Code Consistency

### Style Consistency 
- [x] **Indentation**: 4 spaces (consistent)
- [x] **Braces**: Kernighan & Ritchie style i.e. indentation for '{}' in if-else blocks (consistent)
- [x] **Line length**: Generally <120 characters
- [x] **Import organization**: Proper grouping

### Pattern Consistency 
- [x] **Service methods** follow similar structure:
    1. Validate inputs
    2. Fetch external data
    3. Apply business logic
    4. Return result

- [x] **Controller methods** follow similar structure:
    1. Receive request
    2. Delegate to service
    3. Return ResponseEntity

**Rating**: 5/5 - Excellent consistency

---

## 10. Specific File Reviews

### PointInRegion.java 
**Rating**: 5/5
- **Strengths**: Clear algorithm implementation, well-commented, comprehensive edge case handling
- **Coverage**: 95%
- **Issues**: None found

### PathPlanningService.java 
**Rating**: 4/5
- **Strengths**: Functional implementation, handles simple cases well
- **Issues**: High complexity
- **Coverage**: 81%
- **Recommendation**: Refactor nested loops

### IlpClientService.java 
**Rating**: 3/5
- **Strengths**: Clean RestTemplate usage, configurable endpoint
- **Issues**: No retry logic, timeout handling not tested
- **Coverage**: Likely in 82% service average
- **Recommendation**: Add circuit breaker pattern for resilience

### QueryService.java 
**Rating**: 4/5
- **Strengths**: Dynamic query handling works well, good use of reflection
- **Issues**: Type casting could use better pattern
- **Coverage**: 85%
- **Recommendation**: Consider strategy pattern for extensibility

### CoreRestController.java 
**Rating**: 5/5
- **Strengths**: Clean delegation, proper validation, consistent error handling
- **Coverage**: 91%
- **Issues**: None found

---

## Summary of Review Findings

### Critical Issues (Must Fix)
**None identified** 

### High Priority Issues (Should Fix)
1. PathPlanningService complexity - refactor for maintainability
2. Exception handler coverage - add tests to verify error paths

### Medium Priority Issues (Nice to Have)
3. Extract magic numbers to constants
4. Add defensive null checks in optional field handling
5. Improve Javadoc coverage
6. Consider strategy pattern for QueryService type handling

### Low Priority Issues (Future Enhancement)
7. Add retry logic to IlpClientService
8. Consider caching strategy (if spec allows)
9. Add Swagger/OpenAPI documentation

### Strengths to Maintain
- Excellent separation of concerns
- Consistent naming conventions
- Good use of Java streams
- High test coverage in critical areas
- Clean architectural layering

---

## Compliance with Assignment Requirements

### CW2 Specification Compliance 
- [x] All endpoints start with `/api/v1/` (except actuator/health)
- [x] Uses Java with Spring Boot
- [x] Port 8080 for incoming requests
- [x] JSON handling implemented
- [x] Proper return codes (200, 400, 404)
- [x] Docker image buildable
- [x] Environment variable support (ILP_ENDPOINT)

### Code Quality Standards 
- [x] **Code quality & style (2 pts)**: Readable, clean, commented
- [x] **Good naming (1 pt)**: Meaningful names throughout 
- [x] **Structure (4 pts)**: Excellent SoC, clear layers 
- [x] **Testing/Mocking (3 pts)**: Comprehensive tests, good mocks 

---

## Action Items from Review

### Before Submission
- [ ] Extract `0.00015` to named constant `CLOSE_DISTANCE_THRESHOLD`
- [ ] Add Javadoc to public service methods
- [ ] Verify Docker image builds correctly
- [ ] Run full test suite one final time

### Post-Submission (Future Work)
- [ ] Add exception handler tests
- [ ] Implement retry logic in IlpClientService
- [ ] Add OpenAPI/Swagger documentation
- [ ] Performance profiling for larger datasets

---

## Overall Assessment

**Code Quality**: 4/5 - Very good, production-ready with minor improvements  
**Architecture**: 5/5 - Excellent separation of concerns  
**Testability**: 4/5 - Good coverage, well-structured tests  
**Maintainability**: 4/5 - Clean code, some complexity in path planning  
**Security**: 4/5 - Good practices, no obvious vulnerabilities

**Recommendation**: Code is submission-ready. Identified issues are minor and do not impact correctness or auto-marker compliance.
