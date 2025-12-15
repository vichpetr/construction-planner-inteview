package eu.petrvich.construction.planner.exception;

import eu.petrvich.construction.planner.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for GlobalExceptionHandler.
 * Tests the exception handling through actual HTTP requests to verify error responses.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GlobalExceptionHandlerTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("Should return 400 BAD_REQUEST with proper error for invalid task dependency")
    void testInvalidTaskDependencyException() {
        Task task = new Task();
        task.setTaskCode("T1");
        task.setOperationName("Task 1");
        task.setDuration(10);
        task.setDependencies(List.of("NONEXISTENT_TASK"));

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errorCode").isEqualTo("INVALID_TASK_DEPENDENCY")
                .jsonPath("$.message").value(msg -> assertThat(msg.toString()).contains("NONEXISTENT_TASK"))
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.path").exists();
    }

    @Test
    @DisplayName("Should return 400 BAD_REQUEST with proper error for circular dependency")
    void testCircularDependencyException() {
        Task task1 = new Task();
        task1.setTaskCode("T1");
        task1.setOperationName("Task 1");
        task1.setDuration(10);
        task1.setDependencies(List.of("T2"));

        Task task2 = new Task();
        task2.setTaskCode("T2");
        task2.setOperationName("Task 2");
        task2.setDuration(5);
        task2.setDependencies(List.of("T1"));

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task1, task2))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errorCode").isEqualTo("CIRCULAR_DEPENDENCY")
                .jsonPath("$.message").value(msg -> assertThat(msg.toString().toLowerCase()).contains("circular"))
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.path").exists();
    }

    @Test
    @DisplayName("Should return proper error structure with all required fields")
    void testErrorStructure() {
        Task task = new Task();
        task.setTaskCode("T1");
        task.setOperationName("Task 1");
        task.setDuration(10);
        task.setDependencies(List.of("INVALID"));

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.status").isNumber()
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.errorCode").isNotEmpty()
                .jsonPath("$.path").isNotEmpty();
    }

    @Test
    @DisplayName("Should handle multiple invalid dependencies")
    void testMultipleInvalidDependencies() {
        Task task1 = new Task();
        task1.setTaskCode("T1");
        task1.setOperationName("Task 1");
        task1.setDuration(10);
        task1.setDependencies(List.of("INVALID1"));

        Task task2 = new Task();
        task2.setTaskCode("T2");
        task2.setOperationName("Task 2");
        task2.setDuration(5);
        task2.setDependencies(List.of("INVALID2", "INVALID3"));

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task1, task2))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("INVALID_TASK_DEPENDENCY")
                .jsonPath("$.message").value(msg -> {
                    String message = msg.toString();
                    assertThat(message).contains("INVALID1", "INVALID2", "INVALID3");
                });
    }

    @Test
    @DisplayName("Should handle self-referencing circular dependency")
    void testSelfReferencingDependency() {
        Task task = new Task();
        task.setTaskCode("T1");
        task.setOperationName("Task 1");
        task.setDuration(10);
        task.setDependencies(List.of("T1"));

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("CIRCULAR_DEPENDENCY")
                .jsonPath("$.message").value(msg -> assertThat(msg.toString()).contains("T1"));
    }

    @Test
    @DisplayName("Should return error for complex circular dependency chain")
    void testComplexCircularDependency() {
        Task task1 = new Task();
        task1.setTaskCode("T1");
        task1.setOperationName("Task 1");
        task1.setDuration(10);
        task1.setDependencies(List.of("T2"));

        Task task2 = new Task();
        task2.setTaskCode("T2");
        task2.setOperationName("Task 2");
        task2.setDuration(5);
        task2.setDependencies(List.of("T3"));

        Task task3 = new Task();
        task3.setTaskCode("T3");
        task3.setOperationName("Task 3");
        task3.setDuration(8);
        task3.setDependencies(List.of("T1"));

        webTestClient.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(task1, task2, task3))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("CIRCULAR_DEPENDENCY");
    }
}
