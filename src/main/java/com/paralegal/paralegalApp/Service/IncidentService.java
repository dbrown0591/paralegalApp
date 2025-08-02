package com.paralegal.paralegalApp.Service;

import com.paralegal.paralegalApp.Exceptions.IncidentNotFoundException;
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

    public Incident updateIncident(Long id, Incident updateIncident){

            return incidentRepository.findById(id).map(existingIncident -> {

                updateIncident.setID(id);
                return incidentRepository.save(updateIncident);
            }).orElseThrow(()-> new IncidentNotFoundException("Incident Not Found with Id: " + id));
        }
    @SuppressWarnings("ConstantConditions")
    public Incident partiallyUpdateIncident(Long id, Map<String,Object> updates){
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(()-> new IncidentNotFoundException("Incident Not Found with Id: " + id));

        updates.forEach((key, value)->{
            Field field = ReflectionUtils.findField(Incident.class, key);
            if(field != null){
                field.setAccessible(true);
                ReflectionUtils.setField(field,incident,value);
            }
        });
        return incidentRepository.save(incident);
    }

    public void deleteIncident(Long id){
         incidentRepository.deleteById(id);
    }
}
