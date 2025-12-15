package eu.petrvich.construction.planner.service;

import eu.petrvich.construction.planner.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CpmServiceTest {

    private CpmService cpmService;

    @BeforeEach
    void setUp() {
        cpmService = new CpmService();
    }

    @Test
    @DisplayName("Should calculate CPM for simple linear dependency chain")
    void testSimpleLinearChain() {
        // Task A (5 days) -> Task B (3 days) -> Task C (4 days)
        Task taskA = createTask("A", 5, Collections.emptyList());
        Task taskB = createTask("B", 3, List.of("A"));
        Task taskC = createTask("C", 4, List.of("B"));

        List<Task> tasks = Arrays.asList(taskA, taskB, taskC);

        int projectDuration = cpmService.calculateCriticalPath(tasks);

        // Verify project duration
        assertEquals(12, projectDuration, "Project should take 12 time units");

        // Verify Task A
        assertEquals(0, taskA.getEarliestStart());
        assertEquals(5, taskA.getEarliestFinish());
        assertEquals(0, taskA.getLatestStart());
        assertEquals(5, taskA.getLatestFinish());
        assertEquals(0, taskA.getSlack());
        assertTrue(taskA.isCritical());

        // Verify Task B
        assertEquals(5, taskB.getEarliestStart());
        assertEquals(8, taskB.getEarliestFinish());
        assertEquals(5, taskB.getLatestStart());
        assertEquals(8, taskB.getLatestFinish());
        assertEquals(0, taskB.getSlack());
        assertTrue(taskB.isCritical());

        // Verify Task C
        assertEquals(8, taskC.getEarliestStart());
        assertEquals(12, taskC.getEarliestFinish());
        assertEquals(8, taskC.getLatestStart());
        assertEquals(12, taskC.getLatestFinish());
        assertEquals(0, taskC.getSlack());
        assertTrue(taskC.isCritical());
    }

    @Test
    @DisplayName("Should calculate CPM with parallel tasks")
    void testParallelTasks() {
        /*
         * Task A (4 days) -> Task C (2 days) -> Task E (3 days)
         *                                    /
         * Task B (6 days) -> Task D (5 days)
         */
        Task taskA = createTask("A", 4, Collections.emptyList());
        Task taskB = createTask("B", 6, Collections.emptyList());
        Task taskC = createTask("C", 2, List.of("A"));
        Task taskD = createTask("D", 5, List.of("B"));
        Task taskE = createTask("E", 3, List.of("C", "D"));

        List<Task> tasks = Arrays.asList(taskA, taskB, taskC, taskD, taskE);

        int projectDuration = cpmService.calculateCriticalPath(tasks);

        // Critical path: B (6) -> D (5) -> E (3) = 14 days
        assertEquals(14, projectDuration);

        // Task B, D, E should be on critical path
        assertTrue(taskB.isCritical(), "Task B should be critical");
        assertTrue(taskD.isCritical(), "Task D should be critical");
        assertTrue(taskE.isCritical(), "Task E should be critical");

        // Task A, C should have slack
        assertFalse(taskA.isCritical(), "Task A should not be critical");
        assertFalse(taskC.isCritical(), "Task C should not be critical");
        assertTrue(taskA.getSlack() > 0, "Task A should have slack");
        assertTrue(taskC.getSlack() > 0, "Task C should have slack");
    }

    @Test
    @DisplayName("Should handle tasks with no dependencies")
    void testNoDependencies() {
        Task taskA = createTask("A", 5, Collections.emptyList());
        Task taskB = createTask("B", 3, Collections.emptyList());
        Task taskC = createTask("C", 7, Collections.emptyList());

        List<Task> tasks = Arrays.asList(taskA, taskB, taskC);

        int projectDuration = cpmService.calculateCriticalPath(tasks);

        assertEquals(7, projectDuration, "Project duration should be the longest task");

        // All tasks should start at 0
        assertEquals(0, taskA.getEarliestStart());
        assertEquals(0, taskB.getEarliestStart());
        assertEquals(0, taskC.getEarliestStart());

        // Only the longest task is critical
        assertTrue(taskC.isCritical());
    }

    @Test
    @DisplayName("Should handle single task")
    void testSingleTask() {
        Task task = createTask("A", 10, Collections.emptyList());
        List<Task> tasks = List.of(task);

        int projectDuration = cpmService.calculateCriticalPath(tasks);

        assertEquals(10, projectDuration);
        assertEquals(0, task.getEarliestStart());
        assertEquals(10, task.getEarliestFinish());
        assertTrue(task.isCritical());
    }

    @Test
    @DisplayName("Should handle empty task list")
    void testEmptyTaskList() {
        int projectDuration = cpmService.calculateCriticalPath(Collections.emptyList());
        assertEquals(0, projectDuration);
    }

    @Test
    @DisplayName("Should handle null task list")
    void testNullTaskList() {
        int projectDuration = cpmService.calculateCriticalPath(null);
        assertEquals(0, projectDuration);
    }

    @Test
    @DisplayName("Should set start and end intervals correctly")
    void testIntervals() {
        Task taskA = createTask("A", 5, Collections.emptyList());
        Task taskB = createTask("B", 3, List.of("A"));

        List<Task> tasks = Arrays.asList(taskA, taskB);

        cpmService.calculateCriticalPath(tasks);

        assertEquals(0, taskA.getStartInterval());
        assertEquals(5, taskA.getEndInterval());
        assertEquals(5, taskB.getStartInterval());
        assertEquals(8, taskB.getEndInterval());
    }

    @Test
    @DisplayName("Should handle complex dependency graph")
    void testComplexDependencyGraph() {
        /*
         * More complex scenario:
         * A (3) -> C (4) -> E (2)
         * B (5) -> D (2) -> E (2)
         */
        Task taskA = createTask("A", 3, Collections.emptyList());
        Task taskB = createTask("B", 5, Collections.emptyList());
        Task taskC = createTask("C", 4, List.of("A"));
        Task taskD = createTask("D", 2, List.of("B"));
        Task taskE = createTask("E", 2, List.of("C", "D"));

        List<Task> tasks = Arrays.asList(taskA, taskB, taskC, taskD, taskE);

        int projectDuration = cpmService.calculateCriticalPath(tasks);

        // Expected: max(A->C = 7, B->D = 7) + E = 9
        assertEquals(9, projectDuration);

        // Task E should depend on max finish of C and D
        assertEquals(7, taskE.getEarliestStart());
        assertEquals(9, taskE.getEarliestFinish());
    }

    /**
     * Helper method to create a task for testing.
     */
    private Task createTask(String code, int duration, List<String> dependencies) {
        Task task = new Task();
        task.setTaskCode(code);
        task.setDuration(duration);
        task.setDependencies(dependencies);
        task.setOperationName("Operation " + code);
        task.setElementName("Element " + code);
        return task;
    }
}
