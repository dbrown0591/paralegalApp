package com.paralegal.paralegalApp.Service;

import com.paralegal.paralegalApp.Exceptions.EvidenceNotFoundException;
import com.paralegal.paralegalApp.Model.Evidence;
import com.paralegal.paralegalApp.Repository.EvidenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
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
                        evidence.setId(id);
                        return evidenceRepository.save(evidence);
                    }).orElseThrow(()-> new EvidenceNotFoundException("Evidence Not Found"));
    }
    @SuppressWarnings("ConstantConditions")
    public Evidence partiallyUpdateEvidence(@PathVariable Long id, Map<String, Object> updates){
        Evidence evidence = evidenceRepository.findById(id)
                .orElseThrow(()-> new EvidenceNotFoundException("Evidence not Found by id: " + id));

        updates.forEach((key,value)->{
            Field field = ReflectionUtils.findField(Evidence.class, key);
            if(field != null){
                field.setAccessible(true);
                ReflectionUtils.setField(field, evidence, value);
            }
        });

                return evidenceRepository.save(evidence);
    }

    public void deleteEvidence(Long id){
        evidenceRepository.deleteById(id);
    }
}
