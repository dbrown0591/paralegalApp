package com.paralegal.paralegalApp.MapperTest;

import com.paralegal.paralegalApp.Enum.IncidentStatus;
import com.paralegal.paralegalApp.Enum.SeverityLevel;
import com.paralegal.paralegalApp.Mapper.IncidentMapper;
import com.paralegal.paralegalApp.Model.Incident;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IncidentMapperTest {

    @Test
    void updateEntity_copiesAllFieldsFromSourceToTarget() {
        var source = Incident.builder()
                .reportedBy("Alice")
                .incidentType("TypeA")
                .location("Loc1")
                .description("Desc")
                .severityLevel(SeverityLevel.HIGH)
                .status(IncidentStatus.OPEN)
                .build();

        var target = Incident.builder().build();

        // Act
        IncidentMapper.updateEntity(target, source);

        // Assert
        assertThat(target.getReportedBy()).isEqualTo("Alice");
        assertThat(target.getIncidentType()).isEqualTo("TypeA");
        assertThat(target.getLocation()).isEqualTo("Loc1");
        assertThat(target.getDescription()).isEqualTo("Desc");
        assertThat(target.getSeverityLevel()).isEqualTo(SeverityLevel.HIGH);
        assertThat(target.getStatus()).isEqualTo(IncidentStatus.OPEN);
    }
}
