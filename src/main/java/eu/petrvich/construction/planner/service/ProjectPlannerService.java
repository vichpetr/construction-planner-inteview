package eu.petrvich.construction.planner.service;

import eu.petrvich.construction.planner.config.CacheConfig;
import eu.petrvich.construction.planner.model.ProjectStatistics;
import eu.petrvich.construction.planner.model.Task;
import eu.petrvich.construction.planner.model.TaskWithIntervals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Main service coordinating CPM calculation and project statistics.
 * Thread-safe implementation using ReentrantReadWriteLock for optimal concurrent read performance.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectPlannerService {

    private final CpmService cpmService;
    private final CrewUtilizationService crewUtilizationService;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private ProjectStatistics projectStatistics = new ProjectStatistics(0, 0);
    private List<Task> tasks = new CopyOnWriteArrayList<>();

    /**
     * Gets the current project statistics.
     * Thread-safe operation using read lock (allows concurrent reads).
     * Results are cached to improve performance for repeated calls.
     *
     * @return Current project statistics
     */
    @Cacheable(CacheConfig.PROJECT_STATISTICS_CACHE)
    public ProjectStatistics getProjectStatistics() {
        lock.readLock().lock();
        try {
            log.debug("Retrieving project statistics from source (cache miss)");
            return projectStatistics;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Initializes the project by performing CPM calculation.
     * Thread-safe operation using write lock (exclusive access).
     * Evicts the project statistics cache since data has changed.
     *
     * @param tasks List of tasks to initialize the project with
     */
    @CacheEvict(value = CacheConfig.PROJECT_STATISTICS_CACHE, allEntries = true)
    public void initializeProject(List<Task> tasks) {
        lock.writeLock().lock();
        try {
            log.info("Initializing project planner...");

            // Create a defensive copy to prevent external modification
            List<Task> tasksCopy = new ArrayList<>(tasks);

            int totalDuration = 0;
            int peakCrew = 0;

            if (!tasksCopy.isEmpty()) {
                totalDuration = cpmService.calculateCriticalPath(tasksCopy);
                peakCrew = crewUtilizationService.calculatePeakCrewUtilization(tasksCopy);
            }

            this.tasks = new CopyOnWriteArrayList<>(tasksCopy);
            this.projectStatistics = new ProjectStatistics(totalDuration, peakCrew);

            log.info("Project initialization complete:");
            log.info("  - Total tasks: {}", tasksCopy.size());
            log.info("  - Project duration: {} time units", totalDuration);
            log.info("  - Peak crew utilization: {} crew members", peakCrew);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets all tasks with their calculated start and end intervals.
     * Thread-safe operation using read lock (allows concurrent reads).
     *
     * @return Unmodifiable list of tasks with intervals
     */
    public List<TaskWithIntervals> getTasksWithIntervals() {
        lock.readLock().lock();
        try {
            return tasks.stream()
                    .map(this::convertToTaskWithIntervals)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
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
