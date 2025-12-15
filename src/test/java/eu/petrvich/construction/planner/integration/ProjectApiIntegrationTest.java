package eu.petrvich.construction.planner.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.petrvich.construction.planner.model.ProjectStatistics;
import eu.petrvich.construction.planner.model.Task;
import eu.petrvich.construction.planner.model.TaskWithIntervals;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the Project API endpoints.
 * Tests the full application stack including task registration, CPM calculation, and API responses.
 * Each test loads tasks from tasks.json before execution to ensure test isolation.
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectApiIntegrationTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        // Load tasks from tasks.json and register them
        ClassPathResource tasksResource = new ClassPathResource("tasks.json");
        List<Task> tasks = objectMapper.readValue(
                tasksResource.getInputStream(), new TypeReference<>() {
                }
        );

        // Register tasks via POST /api/tasks
        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tasks)
                .exchange()
                .expectStatus().isOk();
    }

    @AfterEach
    void tearDown() {
        // Clear tasks after each test for isolation
        webTestClient.delete()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("GET /api/project/statistics should return valid project statistics")
    void testGetProjectStatistics() {
        ProjectStatistics statistics = webTestClient.get()
                .uri("/api/project/statistics")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProjectStatistics.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(statistics);

        // Verify the statistics are reasonable
        assertTrue(statistics.getTotalProjectDuration() > 0,
                "Total project duration should be greater than 0");
        assertTrue(statistics.getPeakCrewUtilization() > 0,
                "Peak crew utilization should be greater than 0");

        // Log the actual values for verification
        log.info("Project Statistics:");
        log.info("  Total Duration: {}", statistics.getTotalProjectDuration());
        log.info("  Peak Crew: {}", statistics.getPeakCrewUtilization());
    }

    @Test
    @DisplayName("GET /api/project/tasks should return all tasks with intervals")
    void testGetTasksWithIntervals() {
        List<TaskWithIntervals> tasks = webTestClient.get()
                .uri("/api/project/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskWithIntervals.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(tasks);

        // Verify we have tasks
        assertFalse(tasks.isEmpty(), "Tasks list should not be empty");

        // Verify each task has the required fields
        for (TaskWithIntervals task : tasks) {
            assertNotNull(task.getTaskCode(), "Task code should not be null");
            assertNotNull(task.getOperationName(), "Operation name should not be null");
            assertTrue(task.getDuration() >= 0, "Duration should be non-negative");
            assertTrue(task.getStartInterval() >= 0, "Start interval should be non-negative");
            assertTrue(task.getEndInterval() > 0, "End interval should be positive");
            assertEquals(task.getStartInterval() + task.getDuration(), task.getEndInterval(),
                    "End interval should equal start interval + duration");
        }

        log.info("Total tasks loaded: {}", tasks.size());
        log.info("Sample task: {} [{}-{}]", tasks.getFirst().getTaskCode(), tasks.getFirst().getStartInterval(), tasks.getFirst().getEndInterval());
    }

    @Test
    @DisplayName("GET /api/project/tasks should have tasks with valid dependencies")
    void testTaskDependencies() {
        List<TaskWithIntervals> tasks = webTestClient.get()
                .uri("/api/project/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskWithIntervals.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(tasks);

        var taskMap = tasks.stream()
                .collect(java.util.stream.Collectors.toMap(
                        TaskWithIntervals::getTaskCode,
                        t -> t
                ));

        for (TaskWithIntervals task : tasks) {
            if (task.getDependencies() != null && !task.getDependencies().isEmpty()) {
                for (String depCode : task.getDependencies()) {
                    TaskWithIntervals dependency = taskMap.get(depCode);
                    if (dependency != null) {
                        assertTrue(
                                dependency.getEndInterval() <= task.getStartInterval(),
                                String.format("Task %s should start after dependency %s ends. " +
                                                "Dependency ends at %d, task starts at %d",
                                        task.getTaskCode(), depCode,
                                        dependency.getEndInterval(), task.getStartInterval())
                        );
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("GET /api/project/statistics should be consistent across multiple calls")
    void testStatisticsConsistency() {
        ProjectStatistics stats1 = webTestClient.get()
                .uri("/api/project/statistics")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProjectStatistics.class)
                .returnResult()
                .getResponseBody();

        ProjectStatistics stats2 = webTestClient.get()
                .uri("/api/project/statistics")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProjectStatistics.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(stats1);
        assertNotNull(stats2);

        assertEquals(stats1.getTotalProjectDuration(), stats2.getTotalProjectDuration(), "Project duration should be consistent");
        assertEquals(stats1.getPeakCrewUtilization(), stats2.getPeakCrewUtilization(), "Peak crew utilization should be consistent");
    }

    @Test
    @DisplayName("Tasks should have valid crew assignments")
    void testTaskCrewAssignments() {
        List<TaskWithIntervals> tasks = webTestClient.get()
                .uri("/api/project/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskWithIntervals.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(tasks);

        long tasksWithCrew = tasks.stream()
                .filter(t -> t.getCrew() != null)
                .count();

        long tasksWithoutCrew = tasks.stream()
                .filter(t -> t.getCrew() == null)
                .count();

        log.info("Tasks with crew: {}", tasksWithCrew);
        log.info("Tasks without crew: {}", tasksWithoutCrew);

        for (TaskWithIntervals task : tasks) {
            if (task.getCrew() != null) {
                assertTrue(task.getCrew().getAssignment() >= 0,
                        "Crew assignment should be non-negative");
            }
        }
    }

    @Test
    @DisplayName("GET /api/project/tasks should return tasks in valid time range")
    void testTaskTimeRange() {
        ProjectStatistics statistics = webTestClient.get()
                .uri("/api/project/statistics")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProjectStatistics.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(statistics);

        List<TaskWithIntervals> tasks = webTestClient.get()
                .uri("/api/project/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskWithIntervals.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(tasks);

        for (TaskWithIntervals task : tasks) {
            assertTrue(task.getStartInterval() >= 0,
                    "Task start should be non-negative");
            assertTrue(task.getEndInterval() <= statistics.getTotalProjectDuration(),
                    String.format("Task %s ends at %d which exceeds project duration %d",
                            task.getTaskCode(), task.getEndInterval(),
                            statistics.getTotalProjectDuration()));
        }
    }
}
