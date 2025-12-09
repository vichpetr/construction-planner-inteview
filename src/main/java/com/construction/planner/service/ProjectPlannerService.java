package com.construction.planner.service;

import com.construction.planner.dto.ProjectStatistics;
import com.construction.planner.dto.TaskWithIntervals;
import com.construction.planner.model.Task;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Main service coordinating CPM calculation and project statistics.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectPlannerService {

    private final TaskDataService taskDataService;
    private final CpmService cpmService;
    private final CrewUtilizationService crewUtilizationService;

    private ProjectStatistics projectStatistics;
    private List<Task> calculatedTasks;

    /**
     * Initializes the project by performing CPM calculation on startup.
     */
    @PostConstruct
    public void initializeProject() {
        log.info("Initializing project planner...");

        // Get all tasks
        calculatedTasks = taskDataService.getAllTasks();

        // Calculate critical path
        int totalDuration = cpmService.calculateCriticalPath(calculatedTasks);

        // Calculate peak crew utilization
        int peakCrew = crewUtilizationService.calculatePeakCrewUtilization(calculatedTasks);

        // Store statistics
        projectStatistics = new ProjectStatistics(totalDuration, peakCrew);

        log.info("Project initialization complete:");
        log.info("  - Total tasks: {}", calculatedTasks.size());
        log.info("  - Project duration: {} time units", totalDuration);
        log.info("  - Peak crew utilization: {} crew members", peakCrew);
    }

    /**
     * Gets the project statistics (total duration and peak crew utilization).
     *
     * @return Project statistics
     */
    public ProjectStatistics getProjectStatistics() {
        return projectStatistics;
    }

    /**
     * Gets all tasks with their calculated start and end intervals.
     *
     * @return List of tasks with intervals
     */
    public List<TaskWithIntervals> getTasksWithIntervals() {
        return calculatedTasks.stream()
                .map(this::convertToTaskWithIntervals)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Task to TaskWithIntervals DTO.
     */
    private TaskWithIntervals convertToTaskWithIntervals(Task task) {
        return TaskWithIntervals.builder()
                .taskCode(task.getTaskCode())
                .operationName(task.getOperationName())
                .elementName(task.getElementName())
                .duration(task.getDuration())
                .crew(task.getCrew())
                .equipment(task.getEquipment())
                .dependencies(task.getDependencies())
                .startInterval(task.getStartInterval() != null ? task.getStartInterval() : 0)
                .endInterval(task.getEndInterval() != null ? task.getEndInterval() : 0)
                .build();
    }
}
