package eu.petrvich.construction.planner.service;

import eu.petrvich.construction.planner.exception.CircularDependencyException;
import eu.petrvich.construction.planner.exception.InvalidTaskDependencyException;
import eu.petrvich.construction.planner.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    @DisplayName("Should throw InvalidTaskDependencyException when task depends on non-existent task")
    void testInvalidDependency() {
        Task task = createTask("T1", 10, List.of("NONEXISTENT"));
        List<Task> tasks = List.of(task);

        InvalidTaskDependencyException exception = assertThrows(
                InvalidTaskDependencyException.class,
                () -> cpmService.calculateCriticalPath(tasks),
                "Should throw InvalidTaskDependencyException for non-existent dependency"
        );

        assertTrue(exception.getMessage().contains("T1"),
                "Exception message should mention the task code");
        assertTrue(exception.getMessage().contains("NONEXISTENT"),
                "Exception message should mention the non-existent dependency");
    }

    @Test
    @DisplayName("Should throw InvalidTaskDependencyException for multiple invalid dependencies")
    void testMultipleInvalidDependencies() {
        Task task1 = createTask("T1", 10, List.of("INVALID1"));
        Task task2 = createTask("T2", 5, List.of("INVALID2", "INVALID3"));
        List<Task> tasks = List.of(task1, task2);

        InvalidTaskDependencyException exception = assertThrows(
                InvalidTaskDependencyException.class,
                () -> cpmService.calculateCriticalPath(tasks),
                "Should throw InvalidTaskDependencyException for multiple invalid dependencies"
        );

        assertTrue(exception.getMessage().contains("INVALID1"),
                "Exception should mention first invalid dependency");
        assertTrue(exception.getMessage().contains("INVALID2"),
                "Exception should mention second invalid dependency");
        assertTrue(exception.getMessage().contains("INVALID3"),
                "Exception should mention third invalid dependency");
    }

    @Test
    @DisplayName("Should throw CircularDependencyException for simple circular dependency")
    void testSimpleCircularDependency() {
        Task task1 = createTask("T1", 10, List.of("T2"));
        Task task2 = createTask("T2", 5, List.of("T1"));
        List<Task> tasks = List.of(task1, task2);

        CircularDependencyException exception = assertThrows(
                CircularDependencyException.class,
                () -> cpmService.calculateCriticalPath(tasks),
                "Should throw CircularDependencyException for circular dependency"
        );

        assertTrue(exception.getMessage().toLowerCase().contains("circular"),
                "Exception message should mention circular dependencies");
    }

    @Test
    @DisplayName("Should throw CircularDependencyException for complex circular dependency chain")
    void testComplexCircularDependency() {
        Task task1 = createTask("T1", 10, List.of("T2"));
        Task task2 = createTask("T2", 5, List.of("T3"));
        Task task3 = createTask("T3", 8, List.of("T1"));
        List<Task> tasks = List.of(task1, task2, task3);

        CircularDependencyException exception = assertThrows(
                CircularDependencyException.class,
                () -> cpmService.calculateCriticalPath(tasks),
                "Should throw CircularDependencyException for complex circular dependency chain"
        );

        assertTrue(exception.getMessage().toLowerCase().contains("circular"),
                "Exception message should mention circular dependencies");
    }

    @Test
    @DisplayName("Should throw CircularDependencyException for self-referencing task")
    void testSelfReferencingTask() {
        Task task = createTask("T1", 10, List.of("T1"));
        List<Task> tasks = List.of(task);

        CircularDependencyException exception = assertThrows(
                CircularDependencyException.class,
                () -> cpmService.calculateCriticalPath(tasks),
                "Should throw CircularDependencyException for self-referencing task"
        );

        assertTrue(exception.getMessage().contains("T1"),
                "Exception message should mention the problematic task");
    }

    @Test
    @DisplayName("Should handle tasks with null dependencies gracefully")
    void testNullDependenciesInTask() {
        Task task = createTask("T1", 10, null);

        int duration = cpmService.calculateCriticalPath(List.of(task));

        assertEquals(10, duration, "Should handle null dependencies");
        assertTrue(task.isCritical(), "Task should be critical");
    }

    @Test
    @DisplayName("Should correctly identify non-critical tasks with slack in complex network")
    void testSlackCalculation() {
        /*
         * T1 (10) -> T2 (5)  -> T4 (5)
         *         -> T3 (15) ->
         * T3 is critical path, T2 has slack
         */
        Task task1 = createTask("T1", 10, Collections.emptyList());
        Task task2 = createTask("T2", 5, List.of("T1"));
        Task task3 = createTask("T3", 15, List.of("T1"));
        Task task4 = createTask("T4", 5, List.of("T2", "T3"));

        int duration = cpmService.calculateCriticalPath(List.of(task1, task2, task3, task4));

        assertEquals(30, duration, "Project duration should be 10 + 15 + 5 = 30");
        assertTrue(task1.isCritical(), "Task 1 should be critical");
        assertFalse(task2.isCritical(), "Task 2 should not be critical (has slack)");
        assertTrue(task3.isCritical(), "Task 3 should be critical");
        assertTrue(task4.isCritical(), "Task 4 should be critical");
        assertTrue(task2.getSlack() > 0, "Task 2 should have positive slack");
        assertEquals(10, task2.getSlack(), "Task 2 should have 10 units of slack");
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
