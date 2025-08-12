package com.paralegal.paralegalApp.DTO;

import com.paralegal.paralegalApp.Enum.IncidentStatus;
import com.paralegal.paralegalApp.Enum.SeverityLevel;

import java.time.LocalDateTime;

public record IncidentDTO(Long id,
                          String reportedBy,
                          String incidentType,

                          String location,
                          String description,
                          SeverityLevel severityLevel,
                          IncidentStatus status,
                          LocalDateTime createdAt
) {
}
