package com.paralegal.paralegalApp.ServiceTest;

import com.paralegal.paralegalApp.Enum.IncidentStatus;
import com.paralegal.paralegalApp.Enum.SeverityLevel;
import com.paralegal.paralegalApp.Model.Incident;
import com.paralegal.paralegalApp.Repository.IncidentRepository;
import com.paralegal.paralegalApp.Service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;



import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class IncidentServiceTest {
    @Mock
    IncidentRepository incidentRepository;
    @InjectMocks
    IncidentService incidentService;

    private Incident existing;
    @BeforeEach
    void setUp(){
        existing = Incident.builder()
                .reportedBy("Damion")
                .incidentType("THREAT")
                .severityLevel(SeverityLevel.MEDIUM)
                .status(IncidentStatus.OPEN)
                .location("San Antonio, TX")
                .description("Initial")
                .createdAt(LocalDateTime.now())
                .build();
    }
    @Test
    void getAllIncidents_returnList(){
        when(incidentRepository.findAll()).thenReturn(List.of(existing));
        var result = incidentService.getAllIncidents();
        assertThat(result).hasSize(1);
        verify(incidentRepository).findAll();
    }
    @Test
    void getIncidentById_found(){
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(existing));

        var result = incidentService.getIncidentById(1L);

        assertThat(result).isPresent();
        verify(incidentRepository).findById(1L);
    }
    @Test
    void getIncidentById_notFound(){
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        var result = incidentService.getIncidentById(99L);

        assertThat(result).isEmpty();
    }
    @Test
    void createIncidentSaves(){
       Incident toCreate = Incident.builder()
               .reportedBy("Damion")
               .incidentType("THEFT")
               .severityLevel(SeverityLevel.MEDIUM)
               .status(IncidentStatus.OPEN)
               .location("Jersey City, NJ")
               .description("New")
               .build();
       when(incidentRepository.save(toCreate)).thenReturn(toCreate);

       var saved = incidentService.createIncident(toCreate);

       assertThat(saved).isNotNull();
       verify(incidentRepository).save(toCreate);
    }
}
