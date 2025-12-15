package com.construction.planner.controller;

import com.construction.planner.dto.ProjectStatistics;
import com.construction.planner.dto.TaskWithIntervals;
import com.construction.planner.model.Task;
import com.construction.planner.service.ProjectPlannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for project planning endpoints.
 */
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectPlannerService projectPlannerService;

    /**
     * Returns project statistics including total duration and peak crew utilization.
     *
     * @return Project statistics
     */
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
    @PostMapping("/calculate")
    public ResponseEntity<ProjectStatistics> calculateProject(@RequestBody List<Task> tasks) {
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
    @PostMapping("/calculate/tasks")
    public ResponseEntity<List<TaskWithIntervals>> calculateTasksWithIntervals(@RequestBody List<Task> tasks) {
        log.info("POST /api/project/calculate/tasks with {} tasks", tasks != null ? tasks.size() : 0);
        List<TaskWithIntervals> tasksWithIntervals = projectPlannerService.calculateTasksWithIntervals(tasks);
        return ResponseEntity.ok(tasksWithIntervals);
    }
}
