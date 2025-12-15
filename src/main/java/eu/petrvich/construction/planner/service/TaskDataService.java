package eu.petrvich.construction.planner.service;

import eu.petrvich.construction.planner.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service for loading and managing task data from JSON file or in-memory registration.
 * Supports both file-based initialization and dynamic task registration via API.
 * Thread-safe implementation using ReentrantReadWriteLock for optimal concurrent read performance.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskDataService {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private List<Task> tasks = new CopyOnWriteArrayList<>();
    private final ProjectPlannerService projectPlannerService;

    /**
     * Returns an unmodifiable view of all tasks.
     * Thread-safe operation using read lock (allows concurrent reads).
     *
     * @return Unmodifiable list of all tasks
     */
    public List<Task> getAllTasks() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(tasks);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the number of tasks loaded.
     * Thread-safe operation using read lock (allows concurrent reads).
     *
     * @return Task count
     */
    public int getTaskCount() {
        lock.readLock().lock();
        try {
            return tasks != null ? tasks.size() : 0;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Registers a new list of tasks, replacing any existing tasks.
     * This allows dynamic task registration via API.
     * Thread-safe operation using write lock (exclusive access).
     *
     * @param newTasks List of tasks to register
     * @throws IllegalArgumentException if newTasks is null or empty
     */
    public void registerTasks(List<Task> newTasks) {
        if (newTasks == null || newTasks.isEmpty()) {
            throw new IllegalArgumentException("Task list cannot be null or empty");
        }

        lock.writeLock().lock();
        try {
            if (!CollectionUtils.isEmpty(tasks)) {
                log.info("Registering {} tasks, replacing existing {} tasks", newTasks.size(), tasks.size());
            } else {
                log.info("Registering {} tasks", newTasks.size());
            }

            // Create a copy to prevent external modification
            this.tasks = new CopyOnWriteArrayList<>(new ArrayList<>(newTasks));
            this.projectPlannerService.initializeProject(new ArrayList<>(tasks));
            log.info("Successfully registered {} tasks", tasks.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears all tasks from memory and reinitializes the project.
     * This resets the task list to an empty state and updates project statistics.
     * Thread-safe operation using write lock (exclusive access).
     */
    public void clearTasks() {
        lock.writeLock().lock();
        try {
            int previousSize = tasks.size();
            tasks = new CopyOnWriteArrayList<>();
            projectPlannerService.initializeProject(Collections.emptyList());
            log.info("Cleared {} tasks from memory and reset project statistics", previousSize);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
