package eu.petrvich.construction.planner.service;

import eu.petrvich.construction.planner.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service for loading and managing task data from JSON file or in-memory registration.
 * Supports both file-based initialization and dynamic task registration via API.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskDataService {

    private List<Task> tasks;
    private final ProjectPlannerService projectPlannerService;

    /**
     * Returns all tasks.
     *
     * @return List of all tasks
     */
    public List<Task> getAllTasks() {
        return tasks;
    }

    /**
     * Returns the number of tasks loaded.
     *
     * @return Task count
     */
    public int getTaskCount() {
        return tasks.size();
    }

    /**
     * Registers a new list of tasks, replacing any existing tasks.
     * This allows dynamic task registration via API.
     *
     * @param newTasks List of tasks to register
     * @throws IllegalArgumentException if newTasks is null or empty
     */
    public void registerTasks(List<Task> newTasks) {
        if (newTasks == null || newTasks.isEmpty()) {
            throw new IllegalArgumentException("Task list cannot be null or empty");
        }

        log.info("Registering {} tasks, replacing existing {} tasks", newTasks.size(), tasks.size());
        this.tasks = newTasks;
        this.projectPlannerService.initializeProject(tasks);
        log.info("Successfully registered {} tasks", tasks.size());
    }

    /**
     * Clears all tasks from memory.
     * This resets the task list to an empty state.
     */
    public void clearTasks() {
        int previousSize = tasks.size();
        tasks = Collections.emptyList();
        log.info("Cleared {} tasks from memory", previousSize);
    }
}
