package com.paralegal.paralegalApp.Service;

import com.paralegal.paralegalApp.Exceptions.EvidenceNotFoundException;
import com.paralegal.paralegalApp.Model.Evidence;
import com.paralegal.paralegalApp.Repository.EvidenceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class EvidenceService {


    @Autowired
    private EvidenceRepository evidenceRepository; // TODO: Convert to constructor injection once finalized if not using multiple constuctors

    public List<Evidence> getAllEvidence(){
        return evidenceRepository.findAll();
    }

    public Optional<Evidence> getEvidenceById(Long id){
        return evidenceRepository.findById(id);
    }

    public Evidence createEvidence(Evidence evidence){
        return evidenceRepository.save(evidence);
    }

    public Evidence updateEvidence(Long id, Evidence evidence){

            return  evidenceRepository.findById(id)
                    .map(existingEvidence -> {
                        evidence.setID(id);
                        return evidenceRepository.save(evidence);
                    }).orElseThrow(()-> new EvidenceNotFoundException("Evidence Not Found"));
    }

    public void deleteEvidence(Long id){
        evidenceRepository.deleteById(id);
    }
}
