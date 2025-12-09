package com.construction.planner.service;

import com.construction.planner.model.Crew;
import com.construction.planner.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CrewUtilizationServiceTest {

    private CrewUtilizationService service;

    @BeforeEach
    void setUp() {
        service = new CrewUtilizationService();
    }

    @Test
    @DisplayName("Should calculate peak crew for non-overlapping tasks")
    void testNonOverlappingTasks() {
        // Task A: interval 0-5, crew 3
        Task taskA = createTaskWithCrew("A", 5, 3);
        taskA.setStartInterval(0);

        // Task B: interval 5-10, crew 2
        Task taskB = createTaskWithCrew("B", 5, 2);
        taskB.setStartInterval(5);

        List<Task> tasks = Arrays.asList(taskA, taskB);

        int peakCrew = service.calculatePeakCrewUtilization(tasks);

        // Peak should be 3 (Task A)
        assertEquals(3, peakCrew);
    }

    @Test
    @DisplayName("Should calculate peak crew for overlapping tasks")
    void testOverlappingTasks() {
        // Task A: interval 0-5, crew 3
        Task taskA = createTaskWithCrew("A", 5, 3);
        taskA.setStartInterval(0);

        // Task B: interval 2-7, crew 2
        Task taskB = createTaskWithCrew("B", 5, 2);
        taskB.setStartInterval(2);

        // Task C: interval 3-6, crew 4
        Task taskC = createTaskWithCrew("C", 3, 4);
        taskC.setStartInterval(3);

        List<Task> tasks = Arrays.asList(taskA, taskB, taskC);

        int peakCrew = service.calculatePeakCrewUtilization(tasks);

        // Peak should be 9 (intervals 3-5: A=3 + B=2 + C=4)
        assertEquals(9, peakCrew);
    }

    @Test
    @DisplayName("Should handle tasks with no crew assigned")
    void testTasksWithNoCrew() {
        // Task with crew
        Task taskA = createTaskWithCrew("A", 5, 3);
        taskA.setStartInterval(0);

        // Task without crew
        Task taskB = createTaskWithoutCrew("B", 5);
        taskB.setStartInterval(0);

        List<Task> tasks = Arrays.asList(taskA, taskB);

        int peakCrew = service.calculatePeakCrewUtilization(tasks);

        // Should only count task A's crew
        assertEquals(3, peakCrew);
    }

    @Test
    @DisplayName("Should handle empty task list")
    void testEmptyTaskList() {
        int peakCrew = service.calculatePeakCrewUtilization(Collections.emptyList());
        assertEquals(0, peakCrew);
    }

    @Test
    @DisplayName("Should handle null task list")
    void testNullTaskList() {
        int peakCrew = service.calculatePeakCrewUtilization(null);
        assertEquals(0, peakCrew);
    }

    @Test
    @DisplayName("Should handle single task")
    void testSingleTask() {
        Task task = createTaskWithCrew("A", 10, 5);
        task.setStartInterval(0);

        int peakCrew = service.calculatePeakCrewUtilization(List.of(task));

        assertEquals(5, peakCrew);
    }

    @Test
    @DisplayName("Should calculate crew utilization by interval")
    void testCrewUtilizationByInterval() {
        // Task A: interval 0-3, crew 2
        Task taskA = createTaskWithCrew("A", 3, 2);
        taskA.setStartInterval(0);

        // Task B: interval 2-5, crew 3
        Task taskB = createTaskWithCrew("B", 3, 3);
        taskB.setStartInterval(2);

        List<Task> tasks = Arrays.asList(taskA, taskB);

        Map<Integer, Integer> utilization = service.getCrewUtilizationByInterval(tasks);

        // Interval 0-1: 2 crew (A only)
        assertEquals(2, utilization.get(0));
        assertEquals(2, utilization.get(1));

        // Interval 2: 5 crew (A + B)
        assertEquals(5, utilization.get(2));

        // Interval 3-4: 3 crew (B only)
        assertEquals(3, utilization.get(3));
        assertEquals(3, utilization.get(4));

        // No crew at interval 5+
        assertNull(utilization.get(5));
    }

    @Test
    @DisplayName("Should handle tasks with zero duration")
    void testZeroDurationTask() {
        Task task = createTaskWithCrew("A", 0, 5);
        task.setStartInterval(0);

        int peakCrew = service.calculatePeakCrewUtilization(List.of(task));

        // Zero duration task shouldn't contribute to crew utilization
        assertEquals(0, peakCrew);
    }

    @Test
    @DisplayName("Should handle complex overlapping scenario")
    void testComplexOverlapping() {
        // Multiple tasks with different start times and crews
        Task task1 = createTaskWithCrew("1", 4, 2);
        task1.setStartInterval(0);

        Task task2 = createTaskWithCrew("2", 3, 3);
        task2.setStartInterval(1);

        Task task3 = createTaskWithCrew("3", 2, 1);
        task3.setStartInterval(2);

        Task task4 = createTaskWithCrew("4", 3, 4);
        task4.setStartInterval(3);

        List<Task> tasks = Arrays.asList(task1, task2, task3, task4);

        int peakCrew = service.calculatePeakCrewUtilization(tasks);

        // Interval 3: task1(2) + task2(3) + task3(1) + task4(4) = 10
        assertEquals(10, peakCrew);
    }

    /**
     * Helper method to create a task with crew for testing.
     */
    private Task createTaskWithCrew(String code, int duration, int crewCount) {
        Task task = new Task();
        task.setTaskCode(code);
        task.setDuration(duration);
        task.setCrew(new Crew("Test Crew", crewCount));
        return task;
    }

    /**
     * Helper method to create a task without crew for testing.
     */
    private Task createTaskWithoutCrew(String code, int duration) {
        Task task = new Task();
        task.setTaskCode(code);
        task.setDuration(duration);
        task.setCrew(null);
        return task;
    }
}
