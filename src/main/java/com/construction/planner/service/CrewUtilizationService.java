package com.construction.planner.service;

import com.construction.planner.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating crew utilization throughout the project timeline.
 */
@Service
@Slf4j
public class CrewUtilizationService {

    /**
     * Calculates the peak crew utilization across the entire project.
     * This is the highest sum of crew members utilized at any given time interval.
     *
     * @param tasks List of tasks with calculated start and end intervals
     * @return Peak crew utilization (maximum concurrent crew members)
     */
    public int calculatePeakCrewUtilization(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            log.warn("No tasks provided for crew utilization calculation");
            return 0;
        }

        // Find the project duration
        int projectDuration = tasks.stream()
                .mapToInt(task -> task.getStartInterval() != null ?
                        task.getStartInterval() + task.getDuration() : 0)
                .max()
                .orElse(0);

        if (projectDuration == 0) {
            log.warn("Project duration is 0, no crew utilization");
            return 0;
        }

        // Track crew count at each time interval
        Map<Integer, Integer> crewByInterval = new HashMap<>();

        // For each task, add its crew to all intervals it spans
        for (Task task : tasks) {
            if (task.getStartInterval() == null) {
                log.warn("Task {} has no start interval set", task.getTaskCode());
                continue;
            }

            int start = task.getStartInterval();
            int end = start + task.getDuration();
            int crewCount = task.getCrewCount();

            // Add crew count to each interval the task spans
            for (int interval = start; interval < end; interval++) {
                crewByInterval.merge(interval, crewCount, Integer::sum);
            }
        }

        // Find the peak utilization
        int peakUtilization = crewByInterval.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        log.info("Peak crew utilization: {} crew members", peakUtilization);
        log.info("Total time intervals analyzed: {}", crewByInterval.size());

        return peakUtilization;
    }

    /**
     * Gets crew utilization at each time interval (for debugging/analysis).
     *
     * @param tasks List of tasks with calculated start and end intervals
     * @return Map of interval to crew count
     */
    public Map<Integer, Integer> getCrewUtilizationByInterval(List<Task> tasks) {
        Map<Integer, Integer> crewByInterval = new HashMap<>();

        for (Task task : tasks) {
            if (task.getStartInterval() == null) {
                continue;
            }

            int start = task.getStartInterval();
            int end = start + task.getDuration();
            int crewCount = task.getCrewCount();

            for (int interval = start; interval < end; interval++) {
                crewByInterval.merge(interval, crewCount, Integer::sum);
            }
        }

        return crewByInterval;
    }
}
