package com.construction.planner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @JsonProperty("taskCode")
    private String taskCode;

    @JsonProperty("operationName")
    private String operationName;

    @JsonProperty("elementName")
    private String elementName;

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("crew")
    private Crew crew;

    @JsonProperty("equipment")
    private List<Equipment> equipment = new ArrayList<>();

    @JsonProperty("dependencies")
    private List<String> dependencies = new ArrayList<>();

    // CPM calculation fields
    @JsonIgnore
    private int earliestStart = 0;

    @JsonIgnore
    private int earliestFinish = 0;

    @JsonIgnore
    private int latestStart = 0;

    @JsonIgnore
    private int latestFinish = 0;

    @JsonIgnore
    private int slack = 0;

    @JsonIgnore
    private boolean isCritical = false;

    // For API response (stretch goal)
    @JsonProperty("startInterval")
    private Integer startInterval;

    @JsonProperty("endInterval")
    private Integer endInterval;

    /**
     * Gets the number of crew members assigned to this task.
     * Returns 0 if no crew is assigned.
     */
    public int getCrewCount() {
        return crew != null ? crew.getAssignment() : 0;
    }

    /**
     * Checks if this task has dependencies.
     */
    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
    }
}
