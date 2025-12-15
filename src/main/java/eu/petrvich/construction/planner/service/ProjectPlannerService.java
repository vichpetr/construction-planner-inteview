package eu.petrvich.construction.planner.service;

import eu.petrvich.construction.planner.model.ProjectStatistics;
import eu.petrvich.construction.planner.model.TaskWithIntervals;
import eu.petrvich.construction.planner.model.Task;
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

    private final CpmService cpmService;
    private final CrewUtilizationService crewUtilizationService;

    @Getter
    private ProjectStatistics projectStatistics;
    private List<Task> tasks;

    /**
     * Initializes the project by performing CPM calculation on startup.
     */
    public void initializeProject(List<Task> tasks) {
        log.info("Initializing project planner...");

        this.tasks = tasks;
        int totalDuration = cpmService.calculateCriticalPath(tasks);
        int peakCrew = crewUtilizationService.calculatePeakCrewUtilization(tasks);
        projectStatistics = new ProjectStatistics(totalDuration, peakCrew);

        log.info("Project initialization complete:");
        log.info("  - Total tasks: {}", tasks.size());
        log.info("  - Project duration: {} time units", totalDuration);
        log.info("  - Peak crew utilization: {} crew members", peakCrew);
    }

    /**
     * Gets all tasks with their calculated start and end intervals.
     *
     * @return List of tasks with intervals
     */
    public List<TaskWithIntervals> getTasksWithIntervals() {
        return tasks.stream()
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
