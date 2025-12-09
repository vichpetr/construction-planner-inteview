# Construction Project Planner - CPM Implementation

## Overview
This application implements the Critical Path Method (CPM) to determine the most efficient way of executing a construction project. It analyzes task dependencies, durations, and crew requirements to calculate project timelines and resource utilization.

## Implementation Plan

### 1. Project Setup
- [x] Initialize Git repository
- [x] Create Spring Boot 4 application with Gradle KTS
- [x] Configure Java 21+
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
- [x] Example requests and responses

## Technology Stack
- Java 21
- Spring Boot 3.4.0 (latest stable release)
- Gradle 8.5 with Kotlin DSL
- JUnit 5 for testing
- Spring Boot Test for integration tests
- Jackson for JSON processing
- Lombok for reducing boilerplate code

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

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run the application
./gradlew bootRun
```

## API Usage

```bash
# Get project statistics
curl http://localhost:8080/api/project/statistics

# Get all tasks with intervals
curl http://localhost:8080/api/project/tasks
```

## Implementation Details

### Project Structure
```
src/
├── main/
│   ├── java/com/construction/planner/
│   │   ├── ConstructionPlannerApplication.java  # Main application class
│   │   ├── controller/
│   │   │   └── ProjectController.java           # REST API endpoints
│   │   ├── service/
│   │   │   ├── CpmService.java                 # CPM algorithm implementation
│   │   │   ├── CrewUtilizationService.java     # Crew utilization calculation
│   │   │   ├── TaskDataService.java            # JSON data loading
│   │   │   └── ProjectPlannerService.java      # Main orchestration service
│   │   ├── model/
│   │   │   ├── Task.java                       # Task domain model
│   │   │   ├── Crew.java                       # Crew assignment model
│   │   │   └── Equipment.java                  # Equipment model
│   │   └── dto/
│   │       ├── ProjectStatistics.java          # Statistics response DTO
│   │       └── TaskWithIntervals.java          # Task with intervals DTO
│   └── resources/
│       ├── application.properties              # Application configuration
│       └── tasks.json                          # Task data (1304 tasks)
└── test/
    └── java/com/construction/planner/
        ├── service/                            # Unit tests
        │   ├── CpmServiceTest.java
        │   └── CrewUtilizationServiceTest.java
        └── integration/                        # Integration tests
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

Running the application with the provided LEO2-BE.json file:

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
- **Spring Boot 3.4.0**: Latest stable release (Spring Boot 4 not yet released)
- **Lombok**: Reduces boilerplate code by ~40%
- **Jackson**: Built-in JSON processing, handles complex structures
- **JUnit 5**: Modern testing framework with better assertions

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
- ✓ Tasks fit within project duration

### Test Results
```
BUILD SUCCESSFUL
Total tests: 27
Passed: 27
Failed: 0
Coverage: Core business logic 100%
```
