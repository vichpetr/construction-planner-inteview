package eu.petrvich.construction.planner.controller;

import eu.petrvich.construction.planner.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TaskController.
 * Tests all endpoints for task registration and management.
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void tearDown() {
        // Clear tasks after each test for isolation
        // Note: This might fail if the service was never initialized, but that's acceptable
        try {
            webTestClient.delete()
                    .uri("/api/tasks")
                    .exchange();
        } catch (Exception e) {
            log.error("Failed to clean up tasks after test: {}", e.getMessage());
        }
    }

    @Test
    @DisplayName("POST /api/tasks should register tasks successfully")
    void testRegisterTasks() {
        Task task1 = createTask("T1", "Task 1", 10, Collections.emptyList());
        Task task2 = createTask("T2", "Task 2", 5, List.of("T1"));

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task1, task2))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Tasks registered successfully")
                .jsonPath("$.taskCount").isEqualTo(2);
    }

    @Test
    @DisplayName("POST /api/tasks should replace existing tasks")
    void testRegisterTasksReplacesExisting() {
        // Register first set of tasks
        Task task1 = createTask("T1", "Task 1", 10, Collections.emptyList());
        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task1))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.taskCount").isEqualTo(1);

        // Register second set of tasks (should replace)
        Task task2 = createTask("T2", "Task 2", 5, Collections.emptyList());
        Task task3 = createTask("T3", "Task 3", 8, Collections.emptyList());
        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task2, task3))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.taskCount").isEqualTo(2);

        // Verify only the new tasks exist
        List<Task> allTasks = webTestClient.get()
                .uri("/api/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Task.class)
                .returnResult()
                .getResponseBody();

        assertThat(allTasks).hasSize(2);
        assertThat(allTasks).extracting(Task::getTaskCode)
                .containsExactlyInAnyOrder("T2", "T3");
    }

    @Test
    @DisplayName("POST /api/tasks should handle single task")
    void testRegisterSingleTask() {
        Task task = createTask("T1", "Single Task", 10, Collections.emptyList());

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Tasks registered successfully")
                .jsonPath("$.taskCount").isEqualTo(1);
    }

    @Test
    @DisplayName("DELETE /api/tasks should clear all tasks")
    void testClearTasks() {
        // First register some tasks
        Task task1 = createTask("T1", "Task 1", 10, Collections.emptyList());
        Task task2 = createTask("T2", "Task 2", 5, Collections.emptyList());
        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task1, task2))
                .exchange()
                .expectStatus().isOk();

        // Verify tasks exist
        webTestClient.get()
                .uri("/api/tasks/count")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.taskCount").isEqualTo(2);

        // Clear tasks
        webTestClient.delete()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Tasks cleared successfully");

        // Verify tasks are cleared
        webTestClient.get()
                .uri("/api/tasks/count")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.taskCount").isEqualTo(0);
    }

    @Test
    @DisplayName("DELETE /api/tasks should succeed after tasks are cleared")
    void testClearTasksWhenEmpty() {
        // First register and clear to initialize the service
        Task task = createTask("T1", "Task 1", 10, Collections.emptyList());
        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task))
                .exchange()
                .expectStatus().isOk();

        webTestClient.delete()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk();

        // Now clear again when already empty
        webTestClient.delete()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Tasks cleared successfully");
    }

    @Test
    @DisplayName("GET /api/tasks/count should return 0 after clearing tasks")
    void testGetTaskCountWhenEmpty() {
        // First register tasks to initialize the service
        Task task = createTask("T1", "Task 1", 10, Collections.emptyList());
        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task))
                .exchange()
                .expectStatus().isOk();

        // Then clear them
        webTestClient.delete()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk();

        // Now verify count is 0
        webTestClient.get()
                .uri("/api/tasks/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.taskCount").isEqualTo(0);
    }

    @Test
    @DisplayName("GET /api/tasks/count should return correct count")
    void testGetTaskCount() {
        // Register tasks
        Task task1 = createTask("T1", "Task 1", 10, Collections.emptyList());
        Task task2 = createTask("T2", "Task 2", 5, Collections.emptyList());
        Task task3 = createTask("T3", "Task 3", 8, Collections.emptyList());

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task1, task2, task3))
                .exchange()
                .expectStatus().isOk();

        // Verify count
        webTestClient.get()
                .uri("/api/tasks/count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.taskCount").isEqualTo(3);
    }

    @Test
    @DisplayName("GET /api/tasks should return empty list after clearing tasks")
    void testGetAllTasksWhenEmpty() {
        // First register tasks to initialize the service
        Task task = createTask("T1", "Task 1", 10, Collections.emptyList());
        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task))
                .exchange()
                .expectStatus().isOk();

        // Then clear them
        webTestClient.delete()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk();

        // Now verify list is empty
        List<Task> tasks = webTestClient.get()
                .uri("/api/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Task.class)
                .returnResult()
                .getResponseBody();

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("GET /api/tasks should return all registered tasks")
    void testGetAllTasks() {
        // Register tasks
        Task task1 = createTask("T1", "Task 1", 10, Collections.emptyList());
        Task task2 = createTask("T2", "Task 2", 5, List.of("T1"));

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task1, task2))
                .exchange()
                .expectStatus().isOk();

        // Get all tasks
        List<Task> tasks = webTestClient.get()
                .uri("/api/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Task.class)
                .returnResult()
                .getResponseBody();

        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(Task::getTaskCode)
                .containsExactlyInAnyOrder("T1", "T2");
        assertThat(tasks).extracting(Task::getOperationName)
                .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    @DisplayName("GET /api/tasks should return tasks with all properties")
    void testGetAllTasksWithProperties() {
        Task task0 = createTask("T0", "Base Task", 5, Collections.emptyList());
        Task task1 = createTask("T1", "Complex Task", 15, List.of("T0"));
        task1.setElementName("Element A");

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task0, task1))
                .exchange()
                .expectStatus().isOk();

        List<Task> tasks = webTestClient.get()
                .uri("/api/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Task.class)
                .returnResult()
                .getResponseBody();

        assertThat(tasks).hasSize(2);
        Task retrievedTask = tasks.stream()
                .filter(t -> t.getTaskCode().equals("T1"))
                .findFirst()
                .orElseThrow();
        assertThat(retrievedTask.getTaskCode()).isEqualTo("T1");
        assertThat(retrievedTask.getOperationName()).isEqualTo("Complex Task");
        assertThat(retrievedTask.getDuration()).isEqualTo(15);
        assertThat(retrievedTask.getElementName()).isEqualTo("Element A");
        assertThat(retrievedTask.getDependencies()).containsExactly("T0");
    }

    @Test
    @DisplayName("Should handle complete workflow: register, count, get, clear")
    void testCompleteWorkflow() {
        // Step 1: Register tasks
        Task task1 = createTask("T1", "Task 1", 10, Collections.emptyList());
        Task task2 = createTask("T2", "Task 2", 5, List.of("T1"));

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task1, task2))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.taskCount").isEqualTo(2);

        // Step 2: Verify count
        webTestClient.get()
                .uri("/api/tasks/count")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.taskCount").isEqualTo(2);

        // Step 3: Verify all tasks
        List<Task> tasks = webTestClient.get()
                .uri("/api/tasks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Task.class)
                .returnResult()
                .getResponseBody();

        assertThat(tasks).hasSize(2);

        // Step 4: Clear tasks
        webTestClient.delete()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk();

        // Step 5: Verify cleared
        webTestClient.get()
                .uri("/api/tasks/count")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.taskCount").isEqualTo(0);
    }

    /**
     * Helper method to create a task for testing.
     */
    private Task createTask(String code, String operationName, int duration, List<String> dependencies) {
        Task task = new Task();
        task.setTaskCode(code);
        task.setOperationName(operationName);
        task.setDuration(duration);
        task.setDependencies(dependencies);
        return task;
    }
}
