package com.paralegal.paralegalApp.Repository;

import com.paralegal.paralegalApp.Model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    boolean existsByIdAndReportedBy(Long id, String reportedBy);
}
