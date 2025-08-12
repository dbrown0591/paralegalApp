package com.paralegal.paralegalApp.Controller;

import com.paralegal.paralegalApp.Exceptions.EvidenceNotFoundException;
import com.paralegal.paralegalApp.Model.Evidence;
import com.paralegal.paralegalApp.Service.EvidenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/evidence")
@CrossOrigin("*")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }
    @PostMapping
    public ResponseEntity<Evidence> createEvidence(Evidence evidence){
        Evidence savedEvidence = evidenceService.createEvidence(evidence);
        return new ResponseEntity<>(savedEvidence, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<Evidence>> getAllEvidence(){
        List<Evidence> getEvidence = evidenceService.getAllEvidence();
        return new ResponseEntity<>(getEvidence, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Evidence> getEvidenceById(@PathVariable Long id){
       return evidenceService.getEvidenceById(id).map(evidence -> new ResponseEntity<>(evidence,HttpStatus.OK))
               .orElseThrow(() -> new EvidenceNotFoundException("Evidence Not Found " + id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Evidence> updateEvidence(@PathVariable Long id, @RequestBody Evidence evidence){
        Evidence updateEvidence = evidenceService.updateEvidence(id,evidence);
        return new ResponseEntity<>(updateEvidence, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Evidence> partiallyUpdateEvidence(@PathVariable Long id, @RequestBody Map<String, Object> update){
        Evidence evidence = evidenceService.partiallyUpdateEvidence(id, update);
        return ResponseEntity.ok(evidence);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvidence(@PathVariable Long id){
        evidenceService.deleteEvidence(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
