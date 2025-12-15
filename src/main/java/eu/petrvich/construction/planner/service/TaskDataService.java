package eu.petrvich.construction.planner.service;

import eu.petrvich.construction.planner.model.Task;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for loading and managing task data from JSON file.
 */
@Service
@Slf4j
public class TaskDataService {

    @Value("${app.tasks.data-file}")
    private Resource dataFile;

    private final ObjectMapper objectMapper;
    private List<Task> tasks;

    public TaskDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Loads tasks from the JSON file on application startup.
     */
    @PostConstruct
    public void loadTasks() {
        try {
            log.info("Loading tasks from: {}", dataFile.getFilename());
            tasks = objectMapper.readValue(
                    dataFile.getInputStream(),
                    new TypeReference<List<Task>>() {}
            );
            log.info("Successfully loaded {} tasks", tasks.size());
        } catch (IOException e) {
            log.error("Failed to load tasks from file", e);
            tasks = new ArrayList<>();
        }
    }

    /**
     * Returns all tasks.
     *
     * @return List of all tasks
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Returns the number of tasks loaded.
     *
     * @return Task count
     */
    public int getTaskCount() {
        return tasks.size();
    }
}
