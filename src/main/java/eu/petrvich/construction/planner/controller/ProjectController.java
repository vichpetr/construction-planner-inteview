package eu.petrvich.construction.planner.controller;

import eu.petrvich.construction.planner.model.ProjectStatistics;
import eu.petrvich.construction.planner.model.TaskWithIntervals;
import eu.petrvich.construction.planner.exception.ErrorResponse;
import eu.petrvich.construction.planner.model.Task;
import eu.petrvich.construction.planner.service.ProjectPlannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for project planning endpoints.
 */
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Planning", description = "APIs for CPM calculation and project planning using Critical Path Method")
public class ProjectController {

    private final ProjectPlannerService projectPlannerService;

    /**
     * Returns project statistics including total duration and peak crew utilization.
     *
     * @return Project statistics
     */
    @Operation(
            summary = "Get project statistics",
            description = "Returns project statistics including total duration and peak crew utilization " +
                    "based on registered tasks. Uses CPM algorithm to calculate critical path."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved project statistics",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProjectStatistics.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid task data or dependencies",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/statistics")
    public ResponseEntity<ProjectStatistics> getProjectStatistics() {
        log.info("GET /api/project/statistics");
        ProjectStatistics statistics = projectPlannerService.getProjectStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Returns all tasks with calculated start and end intervals.
     *
     * @return List of tasks with intervals
     */
    @Operation(
            summary = "Get tasks with intervals",
            description = "Returns all tasks with calculated start and end intervals, including earliest/latest times " +
                    "and slack values based on CPM calculation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved tasks with intervals",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskWithIntervals.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid task data or dependencies",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskWithIntervals>> getTasksWithIntervals() {
        log.info("GET /api/project/tasks");
        List<TaskWithIntervals> tasks = projectPlannerService.getTasksWithIntervals();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Calculates project statistics for provided tasks.
     *
     * @param tasks List of tasks to calculate
     * @return Project statistics including duration and peak crew
     */
    @Operation(
            summary = "Calculate project statistics",
            description = "Calculates project statistics for the provided task list without registering them. " +
                    "Returns total project duration and peak crew utilization using CPM algorithm."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully calculated project statistics",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProjectStatistics.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid task list, dependencies, or circular dependencies detected",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/calculate")
    public ResponseEntity<ProjectStatistics> calculateProject(
            @Parameter(description = "List of tasks to calculate project statistics for", required = true)
            @RequestBody List<Task> tasks) {
        log.info("POST /api/project/calculate with {} tasks", tasks != null ? tasks.size() : 0);
        ProjectStatistics statistics = projectPlannerService.calculateProject(tasks);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Calculates project and returns tasks with intervals.
     *
     * @param tasks List of tasks to calculate
     * @return List of tasks with calculated intervals
     */
    @Operation(
            summary = "Calculate tasks with intervals",
            description = "Calculates CPM intervals for the provided task list and returns all tasks with " +
                    "their calculated start/end times, slack values, and critical path indicators."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully calculated tasks with intervals",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskWithIntervals.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid task list, dependencies, or circular dependencies detected",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/calculate/tasks")
    public ResponseEntity<List<TaskWithIntervals>> calculateTasksWithIntervals(
            @Parameter(description = "List of tasks to calculate intervals for", required = true)
            @RequestBody List<Task> tasks) {
        log.info("POST /api/project/calculate/tasks with {} tasks", tasks != null ? tasks.size() : 0);
        List<TaskWithIntervals> tasksWithIntervals = projectPlannerService.calculateTasksWithIntervals(tasks);
        return ResponseEntity.ok(tasksWithIntervals);
    }
}
