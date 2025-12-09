package com.construction.planner.service;

import com.construction.planner.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementing the Critical Path Method (CPM) algorithm.
 *
 * The CPM algorithm determines:
 * - The minimum time required to complete a project
 * - Which tasks are critical (cannot be delayed without delaying the project)
 * - The schedule for each task (earliest and latest start/finish times)
 */
@Service
@Slf4j
public class CpmService {

    /**
     * Calculates the Critical Path Method for the given tasks.
     *
     * @param tasks List of all tasks with dependencies
     * @return The total project duration
     */
    public int calculateCriticalPath(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            log.warn("No tasks provided for CPM calculation");
            return 0;
        }

        // Create a map for quick task lookup
        Map<String, Task> taskMap = tasks.stream()
                .collect(Collectors.toMap(Task::getTaskCode, task -> task));

        // Validate dependencies
        validateDependencies(tasks, taskMap);

        // Perform forward pass to calculate earliest start and finish times
        int projectDuration = performForwardPass(tasks, taskMap);

        // Perform backward pass to calculate latest start and finish times
        performBackwardPass(tasks, taskMap, projectDuration);

        // Calculate slack and identify critical tasks
        calculateSlackAndCriticalPath(tasks);

        // Set start and end intervals for API response
        setIntervals(tasks);

        log.info("CPM calculation complete. Project duration: {} time units", projectDuration);
        log.info("Critical path contains {} tasks",
                tasks.stream().filter(Task::isCritical).count());

        return projectDuration;
    }

    /**
     * Validates that all task dependencies reference existing tasks.
     */
    private void validateDependencies(List<Task> tasks, Map<String, Task> taskMap) {
        for (Task task : tasks) {
            if (task.getDependencies() != null) {
                for (String depCode : task.getDependencies()) {
                    if (!taskMap.containsKey(depCode)) {
                        log.warn("Task {} has dependency on non-existent task {}",
                                task.getTaskCode(), depCode);
                    }
                }
            }
        }
    }

    /**
     * Forward pass: Calculate Earliest Start (ES) and Earliest Finish (EF) for each task.
     * ES = max(EF of all predecessors)
     * EF = ES + duration
     */
    private int performForwardPass(List<Task> tasks, Map<String, Task> taskMap) {
        // Find tasks with no dependencies (starting tasks)
        Set<String> processedTasks = new HashSet<>();
        Queue<Task> queue = new LinkedList<>();

        // Initialize tasks with no dependencies
        for (Task task : tasks) {
            if (!task.hasDependencies()) {
                task.setEarliestStart(0);
                task.setEarliestFinish(task.getDuration());
                queue.offer(task);
                processedTasks.add(task.getTaskCode());
            }
        }

        // Process tasks in dependency order
        while (!queue.isEmpty()) {
            Task currentTask = queue.poll();

            // Find all tasks that depend on this task
            for (Task task : tasks) {
                if (task.getDependencies() != null &&
                    task.getDependencies().contains(currentTask.getTaskCode())) {

                    // Check if all dependencies are processed
                    boolean allDependenciesProcessed = task.getDependencies().stream()
                            .allMatch(processedTasks::contains);

                    if (allDependenciesProcessed && !processedTasks.contains(task.getTaskCode())) {
                        // Calculate ES as max EF of all dependencies
                        int maxEF = task.getDependencies().stream()
                                .map(taskMap::get)
                                .filter(Objects::nonNull)
                                .mapToInt(Task::getEarliestFinish)
                                .max()
                                .orElse(0);

                        task.setEarliestStart(maxEF);
                        task.setEarliestFinish(maxEF + task.getDuration());
                        queue.offer(task);
                        processedTasks.add(task.getTaskCode());
                    }
                }
            }
        }

        // Handle any remaining tasks (circular dependencies or orphaned tasks)
        for (Task task : tasks) {
            if (!processedTasks.contains(task.getTaskCode())) {
                log.warn("Task {} was not processed in forward pass - possible circular dependency",
                        task.getTaskCode());
                task.setEarliestStart(0);
                task.setEarliestFinish(task.getDuration());
            }
        }

        // Project duration is the maximum EF across all tasks
        return tasks.stream()
                .mapToInt(Task::getEarliestFinish)
                .max()
                .orElse(0);
    }

    /**
     * Backward pass: Calculate Latest Start (LS) and Latest Finish (LF) for each task.
     * LF = min(LS of all successors)
     * LS = LF - duration
     */
    private void performBackwardPass(List<Task> tasks, Map<String, Task> taskMap, int projectDuration) {
        // Build successor map (reverse of dependencies)
        Map<String, List<Task>> successorMap = new HashMap<>();
        for (Task task : tasks) {
            successorMap.put(task.getTaskCode(), new ArrayList<>());
        }
        for (Task task : tasks) {
            if (task.getDependencies() != null) {
                for (String depCode : task.getDependencies()) {
                    successorMap.computeIfAbsent(depCode, k -> new ArrayList<>()).add(task);
                }
            }
        }

        // Find tasks with no successors (ending tasks)
        Set<String> processedTasks = new HashSet<>();
        Queue<Task> queue = new LinkedList<>();

        for (Task task : tasks) {
            if (successorMap.get(task.getTaskCode()).isEmpty()) {
                task.setLatestFinish(projectDuration);
                task.setLatestStart(projectDuration - task.getDuration());
                queue.offer(task);
                processedTasks.add(task.getTaskCode());
            }
        }

        // Process tasks in reverse dependency order
        while (!queue.isEmpty()) {
            Task currentTask = queue.poll();

            // Process all tasks that this task depends on
            if (currentTask.getDependencies() != null) {
                for (String depCode : currentTask.getDependencies()) {
                    Task depTask = taskMap.get(depCode);
                    if (depTask != null && !processedTasks.contains(depCode)) {

                        // Check if all successors are processed
                        List<Task> successors = successorMap.get(depCode);
                        boolean allSuccessorsProcessed = successors.stream()
                                .allMatch(s -> processedTasks.contains(s.getTaskCode()));

                        if (allSuccessorsProcessed) {
                            // Calculate LF as min LS of all successors
                            int minLS = successors.stream()
                                    .mapToInt(Task::getLatestStart)
                                    .min()
                                    .orElse(projectDuration);

                            depTask.setLatestFinish(minLS);
                            depTask.setLatestStart(minLS - depTask.getDuration());
                            queue.offer(depTask);
                            processedTasks.add(depCode);
                        }
                    }
                }
            }
        }

        // Handle any remaining tasks
        for (Task task : tasks) {
            if (!processedTasks.contains(task.getTaskCode())) {
                log.warn("Task {} was not processed in backward pass", task.getTaskCode());
                task.setLatestFinish(task.getEarliestFinish());
                task.setLatestStart(task.getEarliestStart());
            }
        }
    }

    /**
     * Calculate slack (float) for each task and identify critical path.
     * Slack = LS - ES (or LF - EF)
     * Critical tasks have slack = 0
     */
    private void calculateSlackAndCriticalPath(List<Task> tasks) {
        for (Task task : tasks) {
            int slack = task.getLatestStart() - task.getEarliestStart();
            task.setSlack(slack);
            task.setCritical(slack == 0);
        }
    }

    /**
     * Set start and end intervals for API response.
     * Using earliest start times for the schedule.
     */
    private void setIntervals(List<Task> tasks) {
        for (Task task : tasks) {
            task.setStartInterval(task.getEarliestStart());
            task.setEndInterval(task.getEarliestFinish());
        }
    }
}
