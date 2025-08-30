package com.paralegal.paralegalApp.ControllerTest;

import com.paralegal.paralegalApp.Controller.IncidentController;
import com.paralegal.paralegalApp.Exceptions.IncidentNotFoundException;
import com.paralegal.paralegalApp.Model.Incident;
import com.paralegal.paralegalApp.Service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import static org.hamcrest.Matchers.hasSize;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class IncidentControllerTest {
    @Mock
    private IncidentService incidentService;

    MockMvc mockMvc;
    @RestControllerAdvice
    static class ApiExceptionHandler{
        @ExceptionHandler(IncidentNotFoundException.class)
        ResponseEntity<String> handle(IncidentNotFoundException ex){
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }
    @BeforeEach
    void setup(){
        var controller =  new IncidentController(incidentService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }
    @Test
    void createIncident_return201_andBody_andLocationHeader() throws Exception{
        var saved = Incident.builder().id(1L).reportedBy("devon@example.com")
                .incidentType("Harassment").location("Office A")
                .description("Details...")
                .createdAt(LocalDateTime.of(2025, 8, 26, 12, 0))
                .build();

        when(incidentService.createIncident(any(Incident.class))).thenReturn(saved);

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                        {
                                "reportedBy" : "devon@example.com",
                                "incidentType" : "Harassment",
                                "location": "Office A",
                                "description" : "Details...",
                                "severityLevel": "LOW",
                                "status": "OPEN"
                                }"""
                        ))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/incidents/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.reportedBy").value("devon@example.com"))
                .andExpect(jsonPath("$.incidentType").value("Harassment"));

        var captor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentService).createIncident(captor.capture());
        var sent = captor.getValue();
        assertThat(sent.getReportedBy()).isEqualTo("devon@example.com");
        assertThat(sent.getIncidentType()).isEqualTo("Harassment");
        assertThat(sent.getLocation()).isEqualTo("Office A");
        assertThat(sent.getDescription()).isEqualTo("Details...");
    }
    @Test
    void createIncident_invalidJson_returns400() throws Exception{
        mockMvc.perform(post("/api/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not-json}"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void createIncident_invalidBody_returns400() throws Exception{
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void getAllIncidents_returns200_andList() throws Exception{
        var a = Incident.builder().id(10L).reportedBy("a@mail.com").incidentType("TypeA").description("d1").build();
        var b = Incident.builder().id(11L).reportedBy("b@mail.com").incidentType("TypeB").description("d2").build();
        when(incidentService.getAllIncidents()).thenReturn(List.of(a,b));

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[1].id").value(11L));
    }
    @Test
    void getIncidentById_found_returns200() throws Exception{
        var found = Incident.builder().id(5L).reportedBy("devon@example.com").incidentType("Harassment").description("x").build();
        when(incidentService.getIncidentById(5L)).thenReturn(Optional.of(found));

        mockMvc.perform(get("/api/incidents/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.reportedBy").value("devon@example.com"));
    }
    @Test
    void getIncidentById_notFound_returns404() throws Exception{
        when(incidentService.getIncidentById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/incidents/404"))
                .andExpect(status().isNotFound());
    }
    @Test
    void updateIncident_put_returns200_andBody() throws Exception{
        var updated = Incident.builder()
                .id(7L)
                .reportedBy("devon@example.com")
                .incidentType("Harassment")
                .location("Office B")
                .description("updated desc")
                .build();

        when(incidentService.updateIncident(Mockito.eq(7L), any(Incident.class))).thenReturn(updated);

        mockMvc.perform(put("/api/incidents/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "reportedBy":"devon@example.com",
                         "incidentType":"Harassment",
                         "location":"Office B",
                         "description":"updated desc",
                         "severityLevel":"LOW",
                         "status":"OPEN"
                         }
                         """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.location").value("Office B"))
                .andExpect(jsonPath("$.description").value("updated desc"));
    }
    @Test
    void updateIncident_put_notFound_returns404() throws Exception{
        when(incidentService.updateIncident(Mockito.eq(404L),any(Incident.class))).thenThrow(new IncidentNotFoundException("Incident Not Found"));

        mockMvc.perform(put("/api/incidents/404")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "reportedBy":"x@mail.com",
                          "incidentType":"Type",
                          "location":"Loc",
                          "description":"Desc",
                          "severityLevel":"LOW",
                          "status":"OPEN"
                        }
                        """))
                .andExpect(status().isNotFound());
    }
    @Test
    void updateIncident_put_invalidBody_returns400() throws Exception{
        mockMvc.perform(put("/api/incidents/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void partiallyUpdateIncident_returns200_andBody() throws Exception{
        var patched = Incident.builder()
                .id(12L)
                .reportedBy("devon@example.com")
                .incidentType("Harassment")
                .location("HQ")
                .description("partially updated")
                .build();

        when(incidentService.partiallyUpdateIncident(Mockito.eq(12L), any(Map.class))).thenReturn(patched);

        mockMvc.perform(patch("/api/incidents/12")
                .contentType(MediaType.APPLICATION_JSON)
                .content( """
                          { "description":"partially updated", "location":"HQ" }
                          """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.description").value("partially updated"))
                .andExpect(jsonPath("$.location").value("HQ"));
    }
    @Test
    void partiallyUpdateIncident_notFound_returns404() throws Exception{
        when(incidentService.partiallyUpdateIncident(Mockito.eq(77L), any(Map.class)))
                .thenThrow(new IncidentNotFoundException("Incident Not Found"));

        mockMvc.perform(patch("/api/incidents/77")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"x\"}"))
                .andExpect(status().isNotFound());
    }
    @Test
    void deleteIncident_returns204() throws Exception{
        doNothing().when(incidentService).deleteIncident(9L);

        mockMvc.perform(delete("/api/incidents/9"))
                .andExpect(status().isNoContent());

        verify(incidentService).deleteIncident(9L);
    }
    @Test
    void deleteIncident_notFound_returns404() throws Exception{
        doThrow(new IncidentNotFoundException("Incident Not Found with Id: 999"))
                .when(incidentService).deleteIncident(999L);

        mockMvc.perform(delete("/api/incidents/999"))
                .andExpect(status().isNotFound());


    }
}
