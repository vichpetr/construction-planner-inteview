package eu.petrvich.construction.planner.controller;

import eu.petrvich.construction.planner.model.ProjectStatistics;
import eu.petrvich.construction.planner.model.TaskWithIntervals;
import eu.petrvich.construction.planner.model.error.ErrorRecord;
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
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/project")
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
                            schema = @Schema(implementation = ErrorRecord.class)
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
                            schema = @Schema(implementation = ErrorRecord.class)
                    )
            )
    })
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskWithIntervals>> getTasksWithIntervals() {
        log.info("GET /api/project/tasks");
        List<TaskWithIntervals> tasks = projectPlannerService.getTasksWithIntervals();
        return ResponseEntity.ok(tasks);
    }
}
