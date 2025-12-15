package eu.petrvich.construction.planner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project statistics including total duration and peak crew utilization")
public class ProjectStatistics {

    @JsonProperty("totalProjectDuration")
    @Schema(
            description = "Total project duration in time units (length of critical path)",
            example = "22",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private int totalProjectDuration;

    @JsonProperty("peakCrewUtilization")
    @Schema(
            description = "Maximum number of crew members needed at any point in time",
            example = "10",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private int peakCrewUtilization;
}
