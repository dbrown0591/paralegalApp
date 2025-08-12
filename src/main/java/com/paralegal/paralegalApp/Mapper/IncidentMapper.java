package com.paralegal.paralegalApp.Mapper;

import com.paralegal.paralegalApp.DTO.IncidentDTO;
import com.paralegal.paralegalApp.Model.Incident;

public class IncidentMapper {
    public static IncidentDTO toDTO(Incident in){
        return new IncidentDTO(
                in.getId(), in.getReportedBy(), in.getIncidentType(),
                in.getLocation(), in.getDescription(),
                in.getSeverityLevel(), in.getStatus(), in.getCreatedAt()
        );
    }

    public static void updateEntity(Incident target, Incident source){
        target.setReportedBy(source.getReportedBy());
        target.setIncidentType(source.getIncidentType());
        target.setLocation(source.getLocation());
        target.setDescription(source.getDescription());
        target.setSeverityLevel(source.getSeverityLevel());
        target.setStatus(source.getStatus());
    }
}
