package com.construction.planner.controller;

import com.construction.planner.dto.ProjectStatistics;
import com.construction.planner.dto.TaskWithIntervals;
import com.construction.planner.service.ProjectPlannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
public class ProjectController {

    private final ProjectPlannerService projectPlannerService;

    /**
     * GET /api/project/statistics
     *
     * Returns project statistics including:
     * - Total project duration (in time units)
     * - Peak crew utilization (maximum concurrent crew members)
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
     * GET /api/project/tasks
     *
     * Returns all tasks with calculated start and end intervals.
     * This is the stretch goal endpoint that includes scheduling information for each task.
     *
     * @return List of tasks with intervals
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskWithIntervals>> getTasksWithIntervals() {
        log.info("GET /api/project/tasks");
        List<TaskWithIntervals> tasks = projectPlannerService.getTasksWithIntervals();
        return ResponseEntity.ok(tasks);
    }
}
