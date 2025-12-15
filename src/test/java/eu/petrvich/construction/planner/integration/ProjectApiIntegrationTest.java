package eu.petrvich.construction.planner.integration;

import eu.petrvich.construction.planner.model.ProjectStatistics;
import eu.petrvich.construction.planner.model.TaskWithIntervals;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Project API endpoints.
 * Tests the full application stack including data loading, CPM calculation, and API responses.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ProjectApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/project/statistics should return valid project statistics")
    void testGetProjectStatistics() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/project/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalProjectDuration").isNumber())
                .andExpect(jsonPath("$.peakCrewUtilization").isNumber())
                .andReturn();

        // Parse the response
        String responseBody = result.getResponse().getContentAsString();
        ProjectStatistics statistics = objectMapper.readValue(responseBody, ProjectStatistics.class);

        // Verify the statistics are reasonable
        assertTrue(statistics.getTotalProjectDuration() > 0,
                "Total project duration should be greater than 0");
        assertTrue(statistics.getPeakCrewUtilization() > 0,
                "Peak crew utilization should be greater than 0");

        // Log the actual values for verification
        System.out.println("Project Statistics:");
        System.out.println("  Total Duration: " + statistics.getTotalProjectDuration());
        System.out.println("  Peak Crew: " + statistics.getPeakCrewUtilization());
    }

    @Test
    @DisplayName("GET /api/project/tasks should return all tasks with intervals")
    void testGetTasksWithIntervals() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/project/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // Parse the response
        String responseBody = result.getResponse().getContentAsString();
        List<TaskWithIntervals> tasks = objectMapper.readValue(
                responseBody,
                new TypeReference<List<TaskWithIntervals>>() {}
        );

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

        System.out.println("Total tasks loaded: " + tasks.size());
        System.out.println("Sample task: " + tasks.get(0).getTaskCode() +
                " [" + tasks.get(0).getStartInterval() + "-" + tasks.get(0).getEndInterval() + "]");
    }

    @Test
    @DisplayName("GET /api/project/tasks should have tasks with valid dependencies")
    void testTaskDependencies() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/project/tasks"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        List<TaskWithIntervals> tasks = objectMapper.readValue(
                responseBody,
                new TypeReference<List<TaskWithIntervals>>() {}
        );

        // Create a map of tasks for dependency validation
        var taskMap = tasks.stream()
                .collect(java.util.stream.Collectors.toMap(
                        TaskWithIntervals::getTaskCode,
                        t -> t
                ));

        // Verify dependencies are satisfied (predecessor finishes before successor starts)
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
    void testStatisticsConsistency() throws Exception {
        // Call the endpoint multiple times
        MvcResult result1 = mockMvc.perform(get("/api/project/statistics"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result2 = mockMvc.perform(get("/api/project/statistics"))
                .andExpect(status().isOk())
                .andReturn();

        // Parse both responses
        String response1 = result1.getResponse().getContentAsString();
        String response2 = result2.getResponse().getContentAsString();

        ProjectStatistics stats1 = objectMapper.readValue(response1, ProjectStatistics.class);
        ProjectStatistics stats2 = objectMapper.readValue(response2, ProjectStatistics.class);

        // They should be identical
        assertEquals(stats1.getTotalProjectDuration(), stats2.getTotalProjectDuration(),
                "Project duration should be consistent");
        assertEquals(stats1.getPeakCrewUtilization(), stats2.getPeakCrewUtilization(),
                "Peak crew utilization should be consistent");
    }

    @Test
    @DisplayName("Tasks should have valid crew assignments")
    void testTaskCrewAssignments() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/project/tasks"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        List<TaskWithIntervals> tasks = objectMapper.readValue(
                responseBody,
                new TypeReference<List<TaskWithIntervals>>() {}
        );

        // Count tasks with and without crew
        long tasksWithCrew = tasks.stream()
                .filter(t -> t.getCrew() != null)
                .count();

        long tasksWithoutCrew = tasks.stream()
                .filter(t -> t.getCrew() == null)
                .count();

        System.out.println("Tasks with crew: " + tasksWithCrew);
        System.out.println("Tasks without crew: " + tasksWithoutCrew);

        // Verify crew assignments are non-negative
        for (TaskWithIntervals task : tasks) {
            if (task.getCrew() != null) {
                assertTrue(task.getCrew().getAssignment() >= 0,
                        "Crew assignment should be non-negative");
            }
        }
    }

    @Test
    @DisplayName("GET /api/project/tasks should return tasks in valid time range")
    void testTaskTimeRange() throws Exception {
        // First get the project statistics to know the total duration
        MvcResult statsResult = mockMvc.perform(get("/api/project/statistics"))
                .andExpect(status().isOk())
                .andReturn();

        ProjectStatistics statistics = objectMapper.readValue(
                statsResult.getResponse().getContentAsString(),
                ProjectStatistics.class
        );

        // Get all tasks
        MvcResult tasksResult = mockMvc.perform(get("/api/project/tasks"))
                .andExpect(status().isOk())
                .andReturn();

        List<TaskWithIntervals> tasks = objectMapper.readValue(
                tasksResult.getResponse().getContentAsString(),
                new TypeReference<List<TaskWithIntervals>>() {}
        );

        // Verify all tasks fit within the project duration
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
