package com.paralegal.paralegalApp.Controller;

import com.paralegal.paralegalApp.DTO.IncidentDTO;
import com.paralegal.paralegalApp.Exceptions.IncidentNotFoundException;
import com.paralegal.paralegalApp.Mapper.IncidentMapper;
import com.paralegal.paralegalApp.Model.Incident;
import com.paralegal.paralegalApp.Service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
//@Validated -- circle back
@RestController
@RequestMapping("/api/incidents")
@CrossOrigin("*")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService){
        this.incidentService = incidentService;
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<IncidentDTO> create(@Valid @RequestBody Incident in) {
        var saved = incidentService.createIncident(in);
        var dto = IncidentMapper.toDTO(saved);
        URI location = URI.create("/api/incidents/" + saved.getId());
        return ResponseEntity.created(location).body(dto); // 201 + Location header
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<IncidentDTO> getAllIncidents(){
       return incidentService.getAllIncidents()
               .stream().map(IncidentMapper::toDTO).toList();
    }
    @PreAuthorize("hasRole('ADMIN') or @incidentAuthz.canView(authentication, #id)")
    @GetMapping("/{id}")
    public IncidentDTO getIncidentById(@PathVariable Long id){
      Incident incident = incidentService.getIncidentById(id)
              .orElseThrow(()-> new IncidentNotFoundException("Incident Not Found with Id: " + id));
      return IncidentMapper.toDTO(incident);
    }
    @PreAuthorize("hasRole('ADMIN') or @incidentAuthz.canEdit(authentication, #id)")
    @PutMapping("/{id}")
    public IncidentDTO updateIncident(@PathVariable Long id, @Valid @RequestBody Incident updateIncident){
        Incident incident = incidentService.updateIncident(id,updateIncident);
        return IncidentMapper.toDTO(incident);
    }
    @PreAuthorize("hasRole('ADMIN') or @incidentAuthz.canEdit(authentication, #id)")
    @PatchMapping("/{id}")
    public IncidentDTO partiallyUpdateIncident(@PathVariable("id") Long incidentId,
                                                            @RequestBody Map<String,Object> updates){
        var updated = incidentService.partiallyUpdateIncident(incidentId, updates);
        return IncidentMapper.toDTO(updated);
    }
    @PreAuthorize("hasRole('ADMIN') or @incidentAuthz.canDelete(authentication, #id)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIncident(@PathVariable Long id){
        incidentService.deleteIncident(id);

    }
}
