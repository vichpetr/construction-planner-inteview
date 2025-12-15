package eu.petrvich.construction.planner.controller;

import eu.petrvich.construction.planner.exception.ErrorResponse;
import eu.petrvich.construction.planner.model.Task;
import eu.petrvich.construction.planner.service.TaskDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing task registration and storage.
 * Provides endpoints to dynamically register and clear tasks in memory.
 */
@RestController
@RequestMapping("/api/tasks")
@Slf4j
@Tag(name = "Task Management", description = "APIs for managing task registration and storage in memory")
public class TaskController {

    private final TaskDataService taskDataService;

    public TaskController(TaskDataService taskDataService) {
        this.taskDataService = taskDataService;
    }

    /**
     * Registers a new list of tasks, replacing any existing tasks in memory.
     * This allows dynamic task registration via API instead of file-based loading.
     *
     * @param tasks List of tasks to register
     * @return Response with success message and task count
     */
    @Operation(
            summary = "Register tasks",
            description = "Registers a new list of tasks, replacing any existing tasks in memory. " +
                    "This allows dynamic task management via API instead of file-based loading."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks registered successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - task list is null or empty",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerTasks(
            @Parameter(description = "List of tasks to register", required = true)
            @RequestBody List<Task> tasks) {
        log.info("POST /api/tasks/register with {} tasks", tasks != null ? tasks.size() : 0);

        taskDataService.registerTasks(tasks);

        return ResponseEntity.ok(Map.of(
                "message", "Tasks registered successfully",
                "taskCount", taskDataService.getTaskCount()
        ));
    }

    /**
     * Clears all tasks from memory.
     * This resets the task storage to an empty state.
     *
     * @return Response with a success message
     */
    @Operation(
            summary = "Clear all tasks",
            description = "Clears all tasks from memory, resetting the task storage to an empty state."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks cleared successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class)
                    )
            )
    })
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearTasks() {
        log.info("DELETE /api/tasks/clear");

        taskDataService.clearTasks();

        return ResponseEntity.ok(Map.of(
                "message", "Tasks cleared successfully"
        ));
    }

    /**
     * Returns the current number of registered tasks.
     *
     * @return Response with task count
     */
    @Operation(
            summary = "Get task count",
            description = "Returns the current number of tasks registered in memory."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved task count",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class)
                    )
            )
    })
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getTaskCount() {
        log.info("GET /api/tasks/count");

        return ResponseEntity.ok(Map.of(
                "taskCount", taskDataService.getTaskCount()
        ));
    }

    /**
     * Returns all currently registered tasks.
     *
     * @return List of all tasks
     */
    @Operation(
            summary = "Get all tasks",
            description = "Returns all tasks currently registered in memory."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all tasks",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Task.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        log.info("GET /api/tasks");

        return ResponseEntity.ok(taskDataService.getAllTasks());
    }
}
