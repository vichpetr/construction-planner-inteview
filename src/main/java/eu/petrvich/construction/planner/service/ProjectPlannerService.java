package eu.petrvich.construction.planner.service;

import eu.petrvich.construction.planner.dto.ProjectStatistics;
import eu.petrvich.construction.planner.dto.TaskWithIntervals;
import eu.petrvich.construction.planner.model.Task;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Getter
    private ProjectStatistics projectStatistics;
    private List<Task> calculatedTasks;

    /**
     * Initializes the project by performing CPM calculation on startup.
     */
    @PostConstruct
    public void initializeProject() {
        log.info("Initializing project planner...");

        calculatedTasks = taskDataService.getAllTasks();
        int totalDuration = cpmService.calculateCriticalPath(calculatedTasks);
        int peakCrew = crewUtilizationService.calculatePeakCrewUtilization(calculatedTasks);
        projectStatistics = new ProjectStatistics(totalDuration, peakCrew);

        log.info("Project initialization complete:");
        log.info("  - Total tasks: {}", calculatedTasks.size());
        log.info("  - Project duration: {} time units", totalDuration);
        log.info("  - Peak crew utilization: {} crew members", peakCrew);
    }

    /**
     * Calculates project statistics for provided tasks.
     *
     * @param tasks List of tasks to calculate
     * @return Project statistics with duration and peak crew
     */
    public ProjectStatistics calculateProject(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Task list cannot be null or empty");
        }

        log.info("Calculating project for {} tasks", tasks.size());

        int totalDuration = cpmService.calculateCriticalPath(tasks);
        int peakCrew = crewUtilizationService.calculatePeakCrewUtilization(tasks);

        log.info("Project calculation complete: duration={}, peakCrew={}", totalDuration, peakCrew);

        return new ProjectStatistics(totalDuration, peakCrew);
    }

    /**
     * Calculates project and returns tasks with intervals.
     *
     * @param tasks List of tasks to calculate
     * @return List of tasks with calculated intervals
     */
    public List<TaskWithIntervals> calculateTasksWithIntervals(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Task list cannot be null or empty");
        }

        log.info("Calculating tasks with intervals for {} tasks", tasks.size());

        cpmService.calculateCriticalPath(tasks);

        return tasks.stream()
                .map(this::convertToTaskWithIntervals)
                .toList();
    }

    /**
     * Gets all tasks with their calculated start and end intervals.
     *
     * @return List of tasks with intervals
     */
    public List<TaskWithIntervals> getTasksWithIntervals() {
        return calculatedTasks.stream()
                .map(this::convertToTaskWithIntervals)
                .toList();
    }

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
