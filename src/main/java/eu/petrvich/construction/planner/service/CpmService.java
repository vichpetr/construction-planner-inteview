package eu.petrvich.construction.planner.service;

import eu.petrvich.construction.planner.exception.CircularDependencyException;
import eu.petrvich.construction.planner.exception.InvalidTaskDependencyException;
import eu.petrvich.construction.planner.model.Task;
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

        Map<String, Task> taskMap = tasks.stream()
                .collect(Collectors.toMap(Task::getTaskCode, task -> task));

        validateDependencies(tasks, taskMap);
        int projectDuration = performForwardPass(tasks, taskMap);
        performBackwardPass(tasks, taskMap, projectDuration);
        calculateSlackAndCriticalPath(tasks);
        setIntervals(tasks);

        log.info("CPM calculation complete. Project duration: {} time units", projectDuration);
        log.info("Critical path contains {} tasks",
                tasks.stream().filter(Task::isCritical).count());

        return projectDuration;
    }

    private void validateDependencies(List<Task> tasks, Map<String, Task> taskMap) {
        List<String> invalidDependencies = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getDependencies() != null) {
                for (String depCode : task.getDependencies()) {
                    if (!taskMap.containsKey(depCode)) {
                        invalidDependencies.add(
                            String.format("Task '%s' depends on non-existent task '%s'",
                                task.getTaskCode(), depCode)
                        );
                    }
                }
            }
        }

        if (!invalidDependencies.isEmpty()) {
            String errorMsg = "Invalid task dependencies detected:\n" +
                String.join("\n", invalidDependencies);
            log.error(errorMsg);
            throw new InvalidTaskDependencyException(errorMsg);
        }
    }

    private int performForwardPass(List<Task> tasks, Map<String, Task> taskMap) {
        Set<String> processedTasks = new HashSet<>();
        Queue<Task> queue = new LinkedList<>();
        Map<String, List<Task>> successorMap = buildSuccessorMap(tasks);

        initializeStartingTasks(tasks, queue, processedTasks);
        processForwardQueue(taskMap, successorMap, queue, processedTasks);
        handleUnprocessedTasksInForwardPass(tasks, processedTasks);

        return calculateProjectDuration(tasks);
    }

    private void initializeStartingTasks(List<Task> tasks, Queue<Task> queue, Set<String> processedTasks) {
        for (Task task : tasks) {
            if (!task.hasDependencies()) {
                task.setEarliestStart(0);
                task.setEarliestFinish(task.getDuration());
                queue.offer(task);
                processedTasks.add(task.getTaskCode());
            }
        }
    }

    private void processForwardQueue(Map<String, Task> taskMap, Map<String, List<Task>> successorMap,
                                     Queue<Task> queue, Set<String> processedTasks) {
        while (!queue.isEmpty()) {
            Task currentTask = queue.poll();
            processTaskSuccessors(taskMap, successorMap, currentTask, queue, processedTasks);
        }
    }

    private void processTaskSuccessors(Map<String, Task> taskMap, Map<String, List<Task>> successorMap,
                                       Task currentTask, Queue<Task> queue, Set<String> processedTasks) {
        List<Task> successors = successorMap.getOrDefault(currentTask.getTaskCode(), Collections.emptyList());

        for (Task successor : successors) {
            if (canProcessTask(successor, processedTasks)) {
                setEarliestTimes(successor, taskMap);
                queue.offer(successor);
                processedTasks.add(successor.getTaskCode());
            }
        }
    }

    private boolean canProcessTask(Task task, Set<String> processedTasks) {
        return processedTasks.containsAll(task.getDependencies()) &&
               !processedTasks.contains(task.getTaskCode());
    }

    private void setEarliestTimes(Task task, Map<String, Task> taskMap) {
        int maxEF = task.getDependencies().stream()
                .map(taskMap::get)
                .filter(Objects::nonNull)
                .mapToInt(Task::getEarliestFinish)
                .max()
                .orElse(0);

        task.setEarliestStart(maxEF);
        task.setEarliestFinish(maxEF + task.getDuration());
    }

    private void handleUnprocessedTasksInForwardPass(List<Task> tasks, Set<String> processedTasks) {
        List<String> unprocessedTasks = tasks.stream()
            .filter(t -> !processedTasks.contains(t.getTaskCode()))
            .map(Task::getTaskCode)
            .toList();

        if (!unprocessedTasks.isEmpty()) {
            String errorMsg = String.format(
                "Unable to calculate project schedule due to circular dependencies or invalid task relationships. " +
                "The following tasks could not be scheduled: %s. " +
                "Please review the task dependencies and ensure there are no circular references.",
                String.join(", ", unprocessedTasks)
            );
            log.error(errorMsg);
            throw new CircularDependencyException(errorMsg);
        }
    }

    private int calculateProjectDuration(List<Task> tasks) {
        return tasks.stream()
                .mapToInt(Task::getEarliestFinish)
                .max()
                .orElse(0);
    }

    private void performBackwardPass(List<Task> tasks, Map<String, Task> taskMap, int projectDuration) {
        Map<String, List<Task>> successorMap = buildSuccessorMap(tasks);
        Set<String> processedTasks = new HashSet<>();
        Queue<Task> queue = new LinkedList<>();

        initializeEndingTasks(tasks, successorMap, queue, processedTasks, projectDuration);
        processBackwardQueue(taskMap, successorMap, queue, processedTasks, projectDuration);
        handleUnprocessedTasksInBackwardPass(tasks, processedTasks);
    }

    private Map<String, List<Task>> buildSuccessorMap(List<Task> tasks) {
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
        return successorMap;
    }

    private void initializeEndingTasks(List<Task> tasks, Map<String, List<Task>> successorMap,
                                      Queue<Task> queue, Set<String> processedTasks, int projectDuration) {
        for (Task task : tasks) {
            if (successorMap.get(task.getTaskCode()).isEmpty()) {
                task.setLatestFinish(projectDuration);
                task.setLatestStart(projectDuration - task.getDuration());
                queue.offer(task);
                processedTasks.add(task.getTaskCode());
            }
        }
    }

    private void processBackwardQueue(Map<String, Task> taskMap, Map<String, List<Task>> successorMap,
                                     Queue<Task> queue, Set<String> processedTasks, int projectDuration) {
        while (!queue.isEmpty()) {
            Task currentTask = queue.poll();
            processTaskPredecessors(currentTask, taskMap, successorMap, queue, processedTasks, projectDuration);
        }
    }

    private void processTaskPredecessors(Task currentTask, Map<String, Task> taskMap,
                                        Map<String, List<Task>> successorMap, Queue<Task> queue,
                                        Set<String> processedTasks, int projectDuration) {
        if (currentTask.getDependencies() == null) return;

        for (String depCode : currentTask.getDependencies()) {
            processPredecessor(depCode, taskMap, successorMap, queue, processedTasks, projectDuration);
        }
    }

    private void processPredecessor(String depCode, Map<String, Task> taskMap,
                                    Map<String, List<Task>> successorMap, Queue<Task> queue,
                                    Set<String> processedTasks, int projectDuration) {
        Task depTask = taskMap.get(depCode);
        if (depTask == null || processedTasks.contains(depCode)) return;

        List<Task> successors = successorMap.get(depCode);
        if (allSuccessorsProcessed(successors, processedTasks)) {
            setLatestTimes(depTask, successors, projectDuration);
            queue.offer(depTask);
            processedTasks.add(depCode);
        }
    }

    private boolean allSuccessorsProcessed(List<Task> successors, Set<String> processedTasks) {
        return successors.stream()
                .allMatch(s -> processedTasks.contains(s.getTaskCode()));
    }

    private void setLatestTimes(Task task, List<Task> successors, int projectDuration) {
        int minLS = successors.stream()
                .mapToInt(Task::getLatestStart)
                .min()
                .orElse(projectDuration);

        task.setLatestFinish(minLS);
        task.setLatestStart(minLS - task.getDuration());
    }

    private void handleUnprocessedTasksInBackwardPass(List<Task> tasks, Set<String> processedTasks) {
        List<String> unprocessedTasks = tasks.stream()
            .filter(t -> !processedTasks.contains(t.getTaskCode()))
            .map(Task::getTaskCode)
            .toList();

        if (!unprocessedTasks.isEmpty()) {
            String errorMsg = String.format(
                "Backward pass failed - the following tasks were not processed: %s. " +
                "This indicates a structural problem in the task graph.",
                String.join(", ", unprocessedTasks)
            );
            log.error(errorMsg);
            throw new CircularDependencyException(errorMsg);
        }
    }

    private void calculateSlackAndCriticalPath(List<Task> tasks) {
        for (Task task : tasks) {
            int slack = task.getLatestStart() - task.getEarliestStart();
            task.setSlack(slack);
            task.setCritical(slack == 0);
        }
    }

    private void setIntervals(List<Task> tasks) {
        for (Task task : tasks) {
            task.setStartInterval(task.getEarliestStart());
            task.setEndInterval(task.getEarliestFinish());
        }
    }
}
