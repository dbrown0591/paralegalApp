package com.paralegal.paralegalApp.ServiceTest;

import com.paralegal.paralegalApp.Enum.IncidentStatus;
import com.paralegal.paralegalApp.Enum.SeverityLevel;
import com.paralegal.paralegalApp.Exceptions.IncidentNotFoundException;
import com.paralegal.paralegalApp.Mapper.IncidentMapper;
import com.paralegal.paralegalApp.Model.Incident;
import com.paralegal.paralegalApp.Repository.IncidentRepository;
import com.paralegal.paralegalApp.Service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static com.paralegal.paralegalApp.Enum.IncidentStatus.OPEN;
import static com.paralegal.paralegalApp.Enum.SeverityLevel.LOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;



import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                .status(OPEN)
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
               .status(OPEN)
               .location("Jersey City, NJ")
               .description("New")
               .build();
       when(incidentRepository.save(toCreate)).thenReturn(toCreate);

       var saved = incidentService.createIncident(toCreate);

       assertThat(saved).isNotNull();
       verify(incidentRepository).save(toCreate);
    }
    @Test
    void updateIncident_callMapperAndSaves(){
        Incident incoming = Incident.builder()
                .reportedBy("D. Brown")
                .incidentType("THREAT")
                .severityLevel(LOW)
                .status(IncidentStatus.CLOSED)
                .description("NJ")
                .build();

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incidentRepository.save(existing)).thenReturn(existing);

        try(var mocked = Mockito.mockStatic(IncidentMapper.class)){
            mocked.when(() -> IncidentMapper.updateEntity(existing,incoming)).thenAnswer(inv -> null);

            var result = incidentService.updateIncident(1L, incoming);

            assertThat(result).isNotNull();
            mocked.verify(() -> IncidentMapper.updateEntity(existing,incoming));
            verify(incidentRepository).save(existing);
        }
    }
    @Test
    void updateIncident_notFound_throws(){
        when(incidentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(IncidentNotFoundException.class, ()-> incidentService.updateIncident(42L, new Incident()));
    }
    @Test
    void partiallyUpdateIncident_updatesSimpleFields(){
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updates = new HashMap<>();
        updates.put("description", "Patched desc");
        updates.put("location", "Austin, TX");

        var patched = incidentService.partiallyUpdateIncident(1L, updates);

        assertThat(patched.getDescription()).isEqualTo("Patched desc");
        assertThat(patched.getLocation()).isEqualTo("Austin, TX");
        verify(incidentRepository).save(existing);
    }
    @Test
    void partiallyUpdateIncident_parsesEnums(){
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updates = Map.of(
                "status", "CLOSED",
                "severityLevel", "HIGH"
        );

        var patched = incidentService.partiallyUpdateIncident(1L, updates);

        assertThat(patched.getStatus()).isEqualTo(IncidentStatus.CLOSED);
        assertThat(patched.getSeverityLevel()).isEqualTo(SeverityLevel.HIGH);

    }
    @Test
    void partiallyUpdateIncident_badEnum_throws400(){
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(existing));

        Map<String, Object> updates = Map.of(
                "status", "NOT_A_STATUS"
        );

        assertThrows(ResponseStatusException.class, ()-> incidentService.partiallyUpdateIncident(1L,updates));
    }

    @Test
    void partiallyUpdateIncident_createdAt_is_immutable() {
        existing.setCreatedAt(LocalDateTime.parse("2025-08-11T10:15:30"));
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> updates = Map.of("createdAt", "2025-08-13T22:45:35"); // attempt to change

        var patched = incidentService.partiallyUpdateIncident(1L, updates);

        assertThat(patched.getCreatedAt())
                .isEqualTo(LocalDateTime.parse("2025-08-11T10:15:30")); // unchanged
    }
    @Test
    void partiallyUpdatedIncident_invalidSeverity_throwsBadRequest(){
        var incident = Incident.builder().id(1L).status(OPEN).severityLevel(LOW).build();
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        Map<String,Object> updates = Map.of("severityLevel", "NOT_A_LEVEL");

        var ex = assertThrows(ResponseStatusException.class,
                () -> incidentService.partiallyUpdateIncident(1L, updates));

        assertEquals("should be 400 BAD_REQUEST",HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void deleteIncident_callsRepo(){
        doNothing().when(incidentRepository).deleteById(5L);

        incidentService.deleteIncident(5L);

        verify(incidentRepository).deleteById(5L);
    }

}
