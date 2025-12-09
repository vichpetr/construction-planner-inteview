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
- [ ] Create Task entity with:
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
- [ ] Parse JSON input file
- [ ] Build task dependency graph
- [ ] Implement forward pass (calculate earliest start/finish times)
- [ ] Implement backward pass (calculate latest start/finish times)
- [ ] Calculate slack/float for each task
- [ ] Identify critical path tasks (slack = 0)
- [ ] Determine project completion time

### 4. Crew Utilization Calculation
- [ ] Track crew assignments over time intervals
- [ ] Calculate total crew members at each time point
- [ ] Determine peak crew utilization

### 5. REST API Endpoints
- [ ] **GET /api/project/statistics**
  - Returns: total project duration and peak crew utilization
  - Response format:
    ```json
    {
      "totalProjectDuration": 120,
      "peakCrewUtilization": 15
    }
    ```

- [ ] **GET /api/project/tasks** (Stretch Goal)
  - Returns: all tasks with computed startInterval and endInterval
  - Response format: Array of tasks with added timing information

### 6. Testing Strategy
- [ ] Unit tests for:
  - CPM algorithm components
  - Dependency graph construction
  - Crew utilization calculation
- [ ] Integration tests with TestContainers:
  - Full API endpoint testing
  - JSON data loading and processing
  - End-to-end workflow validation

### 7. Documentation
- [ ] API documentation
- [ ] Algorithm explanation
- [ ] How to run the application
- [ ] Example requests and responses

## Technology Stack
- Java 21+
- Spring Boot 4.x
- Gradle with Kotlin DSL
- JUnit 5 for testing
- TestContainers for integration tests
- Jackson for JSON processing

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

(To be completed after implementation)

## Design Decisions

(To be completed after implementation)

## Test Coverage

(To be completed after implementation)
