package eu.petrvich.construction.planner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Task {

    @JsonProperty
    private String taskCode;

    @JsonProperty
    private String operationName;

    @JsonProperty
    private String elementName;

    @JsonProperty
    private int duration;

    @JsonProperty
    private Crew crew;

    @JsonProperty
    private List<Equipment> equipment = new ArrayList<>();

    @JsonProperty
    private List<String> dependencies = new ArrayList<>();

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

    @JsonProperty
    private Integer startInterval;

    @JsonProperty
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
