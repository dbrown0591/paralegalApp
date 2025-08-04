package com.paralegal.paralegalApp.Controller;

import com.paralegal.paralegalApp.Exceptions.IncidentNotFoundException;
import com.paralegal.paralegalApp.Model.Incident;
import com.paralegal.paralegalApp.Service.IncidentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/incidents")
@CrossOrigin("*")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService){
        this.incidentService = incidentService;
    }
    @PostMapping
    public ResponseEntity<Incident> createIncident(@RequestBody Incident incident){
        Incident savedIncident = incidentService.createIncident(incident);
        return new ResponseEntity<>(savedIncident, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<Incident>> getAllIncidents(){
       List<Incident> incidents =  incidentService.getAllIncidents();
        return new ResponseEntity<>(incidents, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Incident> getIncidentById(@PathVariable Long id){
        return incidentService.getIncidentById(id)
                .map(incident -> new ResponseEntity<>(incident, HttpStatus.OK))
                .orElseThrow(()->new IncidentNotFoundException("Incident Not Found with Id: " + id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Incident> updateIncident(@PathVariable Long id, @RequestBody Incident updateIncident){
        Incident incident = incidentService.updateIncident(id,updateIncident);
        return new ResponseEntity<>(incident, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Incident> partiallyUpdateIncident(@PathVariable Long id,
                                                            @RequestBody Map<String,Object> updates){
        Incident updateIncident = incidentService.partiallyUpdateIncident(id, updates);
        return ResponseEntity.ok(updateIncident);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id){
        incidentService.deleteIncident(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
