package com.paralegal.paralegalApp.Repository;

import com.paralegal.paralegalApp.Model.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
}
