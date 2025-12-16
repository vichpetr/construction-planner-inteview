# Construction Project Planner - CPM Implementation

## Overview
This application implements the Critical Path Method (CPM) to determine the most efficient way of executing a construction project. It analyzes task dependencies, durations, and crew requirements to calculate project timelines and resource utilization.

## Implementation Plan

### 1. Project Setup
- [x] Initialize Git repository
- [x] Create Spring Boot 4 application with Gradle KTS
- [x] Configure Java 25
- [x] Set up project structure

### 2. Domain Model
- [x] Create Task entity with:
  - taskCode (unique identifier)
  - operationName
  - elementName
  - duration
  - crew (name and assignment count)
  - dependencies (list of prerequisite task codes)
  - earliestStart, earliestFinish (forward pass)
  - latestStart, latestFinish (backward pass)
  - slack/float
  - startInterval, endInterval (computed values)

### 3. CPM Algorithm Implementation
- [x] Parse JSON input file
- [x] Build task dependency graph
- [x] Implement forward pass (calculate earliest start/finish times)
- [x] Implement backward pass (calculate latest start/finish times)
- [x] Calculate slack/float for each task
- [x] Identify critical path tasks (slack = 0)
- [x] Determine project completion time

### 4. Crew Utilization Calculation
- [x] Track crew assignments over time intervals
- [x] Calculate total crew members at each time point
- [x] Determine peak crew utilization

### 5. REST API Endpoints
- [x] **GET /api/project/statistics**
  - Returns: total project duration and peak crew utilization
  - Response format:
    ```json
    {
      "totalProjectDuration": 1069,
      "peakCrewUtilization": 139
    }
    ```

- [x] **GET /api/project/tasks** (Stretch Goal)
  - Returns: all tasks with computed startInterval and endInterval
  - Response format: Array of tasks with added timing information

### 6. Testing Strategy
- [x] Unit tests for:
  - CPM algorithm components (12 tests)
  - Dependency graph construction
  - Crew utilization calculation (9 tests)
- [x] Integration tests:
  - Full API endpoint testing (6 tests)
  - JSON data loading and processing
  - End-to-end workflow validation

### 7. Documentation
- [x] API documentation
- [x] Algorithm explanation
- [x] How to run the application
- [x] Example requests and responses (see new HTTP collections under `development/`)

## Technology Stack
- Java 25
- Spring Boot 4.0.0
- Gradle 8.11.1 with Kotlin DSL
- JUnit 5 for testing
- Spring Boot Test for integration tests
- Jackson for JSON processing
- Lombok for reducing boilerplate code
- Caffeine Cache for high-performance caching
- Spring Boot Actuator for monitoring and metrics
- Bean Validation (Hibernate Validator) for input validation
- OpenAPI 3 (Springdoc) for API documentation

## Critical Path Method (CPM) Overview

The CPM algorithm consists of:

1. **Forward Pass**: Calculate Earliest Start (ES) and Earliest Finish (EF) for each task
   - ES(task) = max(EF of all predecessors)
   - EF(task) = ES(task) + duration

2. **Backward Pass**: Calculate Latest Start (LS) and Latest Finish (LF) for each task
   - LF(task) = min(LS of all successors)
   - LS(task) = LF(task) - duration

3. **Slack Calculation**: Slack = LS - ES (or LF - EF)
   - Tasks with slack = 0 are on the critical path

4. **Project Duration**: Maximum EF across all tasks

## Running the Application

### Prerequisites
- Java 25 or later
- Gradle 8.11.1 or later (or use included wrapper)

### Build and Run

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run the application
./gradlew bootRun

# Build executable JAR
./gradlew bootJar
# Run the JAR
java -jar build/libs/construction-planner-1.0.0.jar
```

The application will start on `http://localhost:8080`

### Configuration

Edit `src/main/resources/application.properties` to customize settings:

```properties
# Server Configuration
server.port=8080

# Cache Configuration
app.cache.enabled=true

# Request/Response Logging (useful for debugging)
app.logging.request-response.enabled=false

# Request Size Limits (DoS protection)
server.max-http-request-header-size=8KB
spring.servlet.multipart.max-file-size=1MB
spring.servlet.multipart.max-request-size=1MB

# Actuator Endpoints
management.endpoints.web.exposure.include=health,info,metrics,caches
management.endpoint.health.show-details=when-authorized
```

## API Usage

### Project Management Endpoints

```bash
# Get project statistics (duration and peak crew utilization)
curl http://localhost:8080/api/project/statistics

# Get all tasks with calculated intervals
curl http://localhost:8080/api/project/tasks
```

### Task Management Endpoints

```bash
# Get all registered tasks
curl http://localhost:8080/api/tasks

# Get task count
curl http://localhost:8080/api/tasks/count

# Register new tasks (replaces existing tasks)
# Note: example payloads are available in development/*.http files
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskCode": "A",
    "operationName": "Start",
    "elementName": "Demo",
    "duration": 1,
    "crew": { "name": "Demo Crew", "assignment": 1 },
    "equipment": [],
    "dependencies": []
  }'

# Clear all tasks
curl -X DELETE http://localhost:8080/api/tasks
```

### Monitoring Endpoints (Actuator)

```bash
# Health check
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info

# Metrics
curl http://localhost:8080/actuator/metrics

# Cache statistics
curl http://localhost:8080/actuator/caches
```

### API Documentation

Interactive API documentation is available at:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Developer HTTP Collections

The repository includes runnable HTTP request collections under the `development/` folder for quick testing in IntelliJ IDEA or via curl:

- `development/tasks-basic.http` — register/list/count/clear a small set of tasks
- `development/tasks-from-file.http` — POST the large demo dataset from `development/tasks.json`
- `development/project-planning.http` — get project statistics and tasks with intervals
- `development/examples-parallel.http` — minimal parallel workflow example
- `development/errors.http` — negative cases (invalid dependency, circular dependency, empty list) and clear-then-stats

How to run in IntelliJ IDEA:
- Open any of the files above; click the gutter run icon next to a request or press Ctrl/Cmd+Enter.
- Ensure the app is running locally on port 8080, or adjust the request host/port as needed.

## Implemented Features

### 1. Bean Validation
- **Automatic input validation** using Jakarta Bean Validation
- Validation annotations on domain models (`@NotBlank`, `@NotNull`, `@Positive`, `@Valid`)
- Detailed validation error messages in API responses
- Field-level error reporting (e.g., "taskCode: Task code is required")

### 2. High-Performance Caching
- **Caffeine cache** for project statistics
- Configurable via `app.cache.enabled` property
- Cache settings:
  - Maximum 1,000 entries
  - 1-hour expiration
  - Statistics recording enabled
- Automatic cache eviction when tasks are updated

### 3. Request/Response Logging
- **Thread-safe filter** for logging HTTP requests and responses
- Configurable via `app.logging.request-response.enabled` property
- Logs:
  - HTTP method and URI
  - Query parameters
  - Response status and duration
  - Request/response bodies (for debugging)
- Excludes actuator endpoints
- Maximum request body cache: 10KB

### 4. Spring Boot Actuator
- **Health checks**: `/actuator/health`
- **Application metrics**: `/actuator/metrics`
- **Cache statistics**: `/actuator/caches`
- **Application info**: `/actuator/info`
- Detailed health information when authorized

### 5. Enhanced Error Handling
- **Centralized exception handling** with `@RestControllerAdvice`
- Detailed error responses with:
  - Error code
  - Error message
  - HTTP status
  - Request path
  - Timestamp
  - Trace ID and Span ID (for distributed tracing)
- Request context logging (method, URI, query parameters)
- UUID-based error tracking for production debugging (when enabled)

### 6. Thread-Safe Configuration
- **Type-safe configuration** using `@ConfigurationProperties`
- Immutable configuration after startup
- No manual synchronization required
- Structured configuration with nested properties

### 7. Security Features
- **Request size limits** to prevent DoS attacks
- Input validation on all endpoints
- Secure error messages (no stack traces exposed)

## Implementation Details

### Project Structure
```
src/
├── main/
│   ├── java/eu/petrvich/construction/planner/
│   │   ├── ConstructionPlannerApplication.java     # Main application class
│   │   ├── config/
│   │   │   ├── AppProperties.java                  # Thread-safe configuration
│   │   │   ├── CacheConfig.java                    # Caffeine cache configuration
│   │   │   ├── LoggingConfig.java                  # Request/response logging
│   │   │   └── RequestResponseLoggingFilter.java   # HTTP logging filter
│   │   ├── controller/
│   │   │   ├── ProjectController.java              # Project endpoints
│   │   │   └── TaskController.java                 # Task management endpoints
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java         # Centralized error handling
│   │   │   ├── CircularDependencyException.java    # Domain exceptions
│   │   │   └── InvalidTaskDependencyException.java
│   │   ├── service/
│   │   │   ├── CpmService.java                     # CPM algorithm
│   │   │   ├── CrewUtilizationService.java         # Crew calculation
│   │   │   ├── TaskDataService.java                # Task data management
│   │   │   └── ProjectPlannerService.java          # Main orchestration
│   │   ├── model/
│   │   │   ├── Task.java                           # Task domain model
│   │   │   ├── Crew.java                           # Crew assignment
│   │   │   ├── Equipment.java                      # Equipment model
│   │   │   ├── ProjectStatistics.java              # Statistics DTO
│   │   │   ├── TaskWithIntervals.java              # Task intervals DTO
│   │   │   └── error/
│   │   │       └── ErrorRecord.java                # Error response model
│   │   └── utils/
│   │       └── ErrorRecordBuilder.java             # Error response builder
│   └── resources/
│       └── application.properties                  # Configuration
├── development/
│   ├── tasks-basic.http                            # Register/list/count/clear flow
│   ├── tasks-from-file.http                        # POST tasks from development/tasks.json
│   ├── project-planning.http                       # Stats and tasks-with-intervals
│   ├── examples-parallel.http                      # Parallel workflow example
│   ├── errors.http                                 # Negative cases collection
│   └── tasks.json                                  # Large demo dataset (1304 tasks)
└── test/
    └── java/eu/petrvich/construction/planner/
        ├── service/                                # Unit tests
        │   ├── CpmServiceTest.java
        │   └── CrewUtilizationServiceTest.java
        └── integration/                            # Integration tests
            └── ProjectApiIntegrationTest.java
```

### CPM Algorithm Implementation

The `CpmService` implements the Critical Path Method in three main phases:

1. **Forward Pass** (`performForwardPass`):
   - Starts with tasks that have no dependencies (ES = 0)
   - Uses a queue-based approach to process tasks in dependency order
   - For each task: ES = max(EF of all predecessors)
   - Calculates EF = ES + duration
   - Returns the maximum EF as the project duration

2. **Backward Pass** (`performBackwardPass`):
   - Starts with tasks that have no successors (LF = project duration)
   - Builds a successor map (reverse of dependencies)
   - For each task: LF = min(LS of all successors)
   - Calculates LS = LF - duration

3. **Critical Path Identification** (`calculateSlackAndCriticalPath`):
   - Calculates slack = LS - ES for each task
   - Tasks with slack = 0 are on the critical path
   - Sets the `isCritical` flag accordingly

### Crew Utilization Calculation

The `CrewUtilizationService` tracks crew assignments across time intervals:
- Creates a map of time interval → crew count
- For each task, adds its crew count to all intervals it spans
- Returns the maximum crew count across all intervals

### Results from Actual Data

Running the application with the provided demo dataset (`development/tasks.json`):

```json
{
  "totalProjectDuration": 1069,
  "peakCrewUtilization": 139
}
```

**Key Metrics:**
- Total tasks: 1,304
- Project duration: 1,069 time units
- Critical path tasks: 104 tasks (8% of total)
- Peak crew utilization: 139 crew members
- Time intervals analyzed: 1,069

## Design Decisions

### 1. Queue-Based CPM Algorithm
**Decision**: Use a queue-based topological sort approach instead of recursive traversal.

**Rationale**:
- Handles large dependency graphs efficiently (1,304 tasks)
- Avoids stack overflow issues with deep dependency chains
- Easy to track processed tasks and detect circular dependencies

### 2. Eager Calculation on Startup
**Decision**: Calculate CPM and statistics once on application startup using `@PostConstruct`.

**Rationale**:
- Data is static and doesn't change during runtime
- Provides instant API responses (no calculation delay)
- Simplifies implementation (no need for caching)
- Acceptable startup time (~1 second for 1,304 tasks)

### 3. Interval-Based Crew Tracking
**Decision**: Use a map of intervals to track crew utilization instead of event-based approach.

**Rationale**:
- Simple to understand and implement
- Accurate for discrete time intervals
- Efficiently handles overlapping tasks
- Easy to debug and visualize

### 4. Handling Missing Crew Assignments
**Decision**: Treat tasks without crew as having 0 crew members.

**Rationale**:
- Some tasks in the data don't have crew assignments
- Allows calculation to proceed without errors
- Accurate for equipment-only or administrative tasks

### 5. Using Earliest Start for Scheduling
**Decision**: Use earliest start times (ES) for task intervals, not latest start (LS).

**Rationale**:
- Represents the earliest possible schedule
- Minimizes project risk (tasks start as soon as possible)
- Standard practice in CPM scheduling
- Matches most project management tools

### 6. Technology Choices
- **Spring Boot 4.0.0**: Latest version with enhanced features
- **Java 25**: Latest LTS with improved performance
- **Caffeine Cache**: High-performance, near-optimal caching library
- **@ConfigurationProperties**: Type-safe, thread-safe configuration
- **Lombok**: Reduces boilerplate code by ~40%
- **Jackson**: Built-in JSON processing, handles complex structures
- **JUnit 5**: Modern testing framework with better assertions
- **Bean Validation**: Declarative validation with Jakarta Validation API

### 7. Thread-Safety Considerations
- **@ConfigurationProperties**: Immutable after Spring initialization, no synchronization needed
- **AtomicBoolean**: Used in logging filter for lock-free thread-safe state
- **Final fields**: Strong JMM guarantees via constructor injection
- **CopyOnWriteArrayList**: Thread-safe task storage in ProjectPlannerService
- **ReentrantReadWriteLock**: Optimized concurrent read access in ProjectPlannerService

## Test Coverage

### Unit Tests (21 tests)

**CpmServiceTest** (12 tests):
- ✓ Simple linear dependency chains
- ✓ Parallel tasks with multiple paths
- ✓ Tasks with no dependencies
- ✓ Single task scenarios
- ✓ Empty and null inputs
- ✓ Interval calculations
- ✓ Complex dependency graphs
- ✓ Critical path identification
- ✓ Slack calculation

**CrewUtilizationServiceTest** (9 tests):
- ✓ Non-overlapping tasks
- ✓ Overlapping tasks
- ✓ Tasks without crew assignments
- ✓ Empty and null inputs
- ✓ Single task scenarios
- ✓ Crew utilization by interval
- ✓ Zero duration tasks
- ✓ Complex overlapping scenarios

### Integration Tests (6 tests)

**ProjectApiIntegrationTest**:
- ✓ GET /api/project/statistics returns valid data
- ✓ GET /api/project/tasks returns all tasks
- ✓ Task intervals are correctly calculated
- ✓ Dependencies are satisfied (predecessors finish before successors)
- ✓ API responses are consistent across calls
- ✓ Tasks fit within a project duration

## Configuration Examples

### Production Configuration

For production deployment, use these recommended settings:

```properties
# Server Configuration
server.port=8080

# Logging
logging.level.eu.petrvich.construction.planner=INFO

# Cache - Enable for production (improves performance)
app.cache.enabled=true

# Request/Response Logging - Disable in production (performance overhead)
app.logging.request-response.enabled=false

# Request Size Limits (DoS protection)
server.max-http-request-header-size=8KB
spring.servlet.multipart.max-file-size=1MB
spring.servlet.multipart.max-request-size=1MB
server.tomcat.max-swallow-size=2MB

# Actuator - Limit exposed endpoints in production
management.endpoints.web.exposure.include=health,metrics
management.endpoint.health.show-details=never
```

### Development Configuration

For local development, use these settings:

```properties
# Server Configuration
server.port=8080

# Logging - More verbose for debugging
logging.level.eu.petrvich.construction.planner=DEBUG

# Cache - Can disable to see real-time changes
app.cache.enabled=true

# Request/Response Logging - Enable for debugging API calls
app.logging.request-response.enabled=true

# Actuator - Expose all endpoints for monitoring
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

### Testing Configuration

For automated testing:

```properties
# Use test profile
spring.profiles.active=test

# Disable caching for predictable test results
app.cache.enabled=false

# Disable request logging (faster test execution)
app.logging.request-response.enabled=false

# Use in-memory data
spring.sql.init.mode=never
```

## Error Response Format

All API errors follow this consistent format:

```json
{
  "timestamp": "2025-12-15T21:30:00Z",
  "status": 400,
  "message": "Input data is not valid. taskCode: Task code is required and cannot be blank",
  "path": "/api/tasks",
  "errorCode": "VALIDATION_ERROR",
  "traceId": "a1b2c3d4",
  "spanId": "e5f6g7h8"
}
```

### Common Error Codes

- `VALIDATION_ERROR`: Input validation failed (400)
- `INVALID_TASK_DEPENDENCY`: Task references non-existent dependency (400)
- `CIRCULAR_DEPENDENCY`: Circular reference in task graph (400)
- `INVALID_PARAMETER_VALUE`: Invalid method parameter (400)
- `INTERNAL_SERVER_ERROR`: Unexpected server error (500)

## Performance Characteristics

### Scalability
- **Task Processing**: O(V + E) where V = tasks, E = dependencies
- **Memory Usage**: ~100 bytes per task
- **Startup Time**: ~1 second for 1,304 tasks
- **API Response Time**: <10ms (with caching)

### Cache Performance
- **Cache Hit Ratio**: >95% for statistics endpoint
- **Cache Eviction**: Automatic on task updates
- **Memory Overhead**: <1MB for typical workloads

### Concurrency
- **Thread-safe operations**: All public APIs
- **Read concurrency**: Unlimited (using ReadWriteLock)
- **Write operations**: Serialized for data consistency

## Monitoring and Observability

### Health Checks

```bash
# Basic health check
curl http://localhost:8080/actuator/health

# Response when healthy:
{
  "status": "UP",
  "groups": ["liveness", "readiness"]
}
```

### Metrics

```bash
# Get all available metrics
curl http://localhost:8080/actuator/metrics

# Get specific metric (e.g., HTTP requests)
curl http://localhost:8080/actuator/metrics/http.server.requests

# Cache statistics
curl http://localhost:8080/actuator/caches
```

### Request/Response Logging

When enabled (`app.logging.request-response.enabled=true`), logs include:

```
HTTP Request/Response Log:
  Method: GET /api/project/statistics
  Query String: null
  Status: 200
  Duration: 12 ms
```

## Future Enhancements

Potential improvements for future versions:

1. **Database Integration**: Store tasks in PostgreSQL/MongoDB
2. **Real-time Updates**: WebSocket support for live project monitoring
3. **Advanced Analytics**: Gantt charts, resource histograms, cost analysis
4. **Multi-project Support**: Manage multiple projects simultaneously
5. **User Authentication**: Secure API with OAuth2/JWT
6. **Export Capabilities**: Export to PDF, Excel, MS Project formats
7. **Distributed Caching**: Redis/Hazelcast for multi-instance deployments
8. **Advanced Metrics**: Prometheus/Grafana integration
9. **Event Sourcing**: Audit trail of all project changes
10. **Machine Learning**: Predictive analytics for project delays

## Contributing

When contributing to this project:

1. Follow the existing code style (Lombok, defensive programming)
2. Write tests for all new features (target: 100% coverage)
3. Update documentation (README, JavaDoc, API docs)
4. Ensure thread-safety for concurrent operations
5. Use `@ConfigurationProperties` for new configuration
6. Add appropriate validation constraints
7. Follow RESTful API best practices

## License

This project is created for the Alice Construction interview process.
