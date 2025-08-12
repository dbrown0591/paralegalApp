package com.paralegal.paralegalApp.Service;

import com.paralegal.paralegalApp.Enum.IncidentStatus;
import com.paralegal.paralegalApp.Enum.SeverityLevel;
import com.paralegal.paralegalApp.Exceptions.IncidentNotFoundException;
import com.paralegal.paralegalApp.Mapper.IncidentMapper;
import com.paralegal.paralegalApp.Model.Incident;
import com.paralegal.paralegalApp.Repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class IncidentService {
    @Autowired
    private IncidentRepository incidentRepository; // TODO: Convert to constructor injection once finalized if not using multiple constuctors

    public List<Incident> getAllIncidents(){
        return incidentRepository.findAll();
    }

    public Optional<Incident> getIncidentById(Long id){
        return incidentRepository.findById(id);
    }

    public Incident createIncident(Incident incident){
        return incidentRepository.save(incident);
    }

    // inside IncidentService
    public Incident updateIncident(Long id, Incident incoming) {
        Incident existing = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident Not Found with Id: " + id));
        IncidentMapper.updateEntity(existing, incoming);
        return incidentRepository.save(existing);
    }

    @SuppressWarnings("ConstantConditions")
    public Incident partiallyUpdateIncident(Long id, Map<String, Object> updates) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident Not Found with Id: " + id));

        updates.forEach((key, value) -> {
            switch (key) {
                case "status" -> {
                    try {
                        var v = IncidentStatus.valueOf(value.toString());
                        incident.setStatus(v);
                    } catch (IllegalArgumentException e) {
                        throw new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid status value");
                    }
                }
                case "severityLevel" -> {
                    try {
                        var v = SeverityLevel.valueOf(value.toString());
                        incident.setSeverityLevel(v);
                    } catch (IllegalArgumentException e) {
                        throw new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid severityLevel value");
                    }
                }
                case "createdAt" -> {
                    // optional: allow updating createdAt if you want
                    incident.setCreatedAt(java.time.LocalDateTime.parse(value.toString()));
                }
                default -> {
                    var field = org.springframework.util.ReflectionUtils.findField(Incident.class, key);
                    if (field != null) {
                        field.setAccessible(true);
                        org.springframework.util.ReflectionUtils.setField(field, incident, value);
                    }
                }
            }
        });

        return incidentRepository.save(incident);
    }


    public void deleteIncident(Long id){
         incidentRepository.deleteById(id);
    }
}
