package com.paralegal.paralegalApp.ControllerTest;

import com.paralegal.paralegalApp.Controller.EvidenceController;
import com.paralegal.paralegalApp.Exceptions.EvidenceNotFoundException;
import com.paralegal.paralegalApp.Model.Evidence;
import com.paralegal.paralegalApp.Service.EvidenceService;
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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class EvidenceControllerTest {

    private MockMvc mockMvc;
    @Mock
    private EvidenceService evidenceService;

    @RestControllerAdvice
    static class ApiExceptionHandler {
        @ExceptionHandler(EvidenceNotFoundException.class)
        ResponseEntity<String> handle(EvidenceNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @BeforeEach
    void setup() {
        var controller = new EvidenceController(evidenceService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }


    @Test
    void getAllEvidence_returns200AndBody() throws Exception {
        var e1 = Evidence.builder().Id(1L).fileName("a").fileType("image/png").build();
        var e2 = Evidence.builder().Id(2L).fileName("b").fileType("image/jpeg").build();
        when(evidenceService.getAllEvidence()).thenReturn(List.of(e1,e2));

        mockMvc.perform(get("/api/evidence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].fileName").value("a"));
    }
    @Test
    void getEvidenceById_found_returns200() throws Exception{
        var e1 = Evidence.builder().Id(10L).fileName("a").build();
        when(evidenceService.getEvidenceById(10L)).thenReturn(Optional.of(e1));

        mockMvc.perform(get("/api/evidence/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.fileName").value("a"));
    }
    @Test
    void createEvidence_returns201() throws Exception {
        var created = Evidence.builder().Id(5L).fileName("new").fileType("image/png").build();
        when(evidenceService.createEvidence(any(Evidence.class))).thenReturn(created);

        mockMvc.perform(post("/api/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"new\",\"fileType\":\"image/png\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.fileName").value("new"));
    }
    @Test
    void updateEvidence_returns200() throws Exception{
        var updated = Evidence.builder().Id(7L).fileName("up").fileType("image/png").build();
        when(evidenceService.updateEvidence(Mockito.eq(7L), any(Evidence.class))).thenReturn(updated);

        mockMvc.perform(put("/api/evidence/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"up\",\"fileType\":\"image/png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath(".fileName").value("up"));
    }

    @Test
    void partiallyUpdateEvidence_returns200() throws Exception{

        var patched = Evidence.builder().Id(8L).fileName("patched").fileType("image/jpeg").build();
        when(evidenceService.partiallyUpdateEvidence(Mockito.eq(8L), any())).thenReturn(patched);

        mockMvc.perform(patch("/api/evidence/8")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"patched\",\"fileType\":\"image/jpeg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8L))
                .andExpect(jsonPath("$.fileName").value("patched"))
                .andExpect(jsonPath("$.fileType").value("image/jpeg"));
    }
    @Test
    void deleteEvidence_returns204() throws Exception{
        mockMvc.perform(delete("/api/evidence/9"))
                .andExpect(status().isNoContent());
    }
    @Test
    void getEvidenceById_notFound_returns404() throws Exception{
        when(evidenceService.getEvidenceById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/evidence/404"))
                .andExpect(status().isNotFound());
    }
    @Test
    void updateEvidence_notFound_returns404() throws Exception{
        when(evidenceService.updateEvidence(Mockito.eq(99L), any(Evidence.class)))
                .thenThrow(new EvidenceNotFoundException("Evidence Not Found"));

        mockMvc.perform(put("/api/evidence/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"x\"}"))
                .andExpect(status().isNotFound());
    }
    @Test
    void partiallyUpdateEvidence_notFound_returns404() throws Exception{
        when(evidenceService.partiallyUpdateEvidence(Mockito.eq(77L), any()))
                .thenThrow(new EvidenceNotFoundException("Evidence Not Found"));

        mockMvc.perform(patch("/api/evidence/77")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"x\"}"))
                .andExpect(status().isNotFound());
    }
    @Test
    void createEvidence_missingBody_returns400() throws Exception{
        mockMvc.perform(post("/api/evidence")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    @Test
    void createEvidence_invalidJson_returns400() throws Exception{
        mockMvc.perform(post("/api/evidence")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not-json"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void updateEvidence_withoutContentType_returns415() throws Exception{
        mockMvc.perform(put("/api/evidence/1")
                .content("{\"fileName\":\"x\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void createEvidence_mapsRequestBody_toService() throws Exception {
        var created = Evidence.builder().Id(10L).fileName("new").fileType("image/png").build();
        when(evidenceService.createEvidence(any(Evidence.class))).thenReturn(created);

        mockMvc.perform(post("/api/evidence")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"new\",\"fileType\":\"image/png\"}"))
                .andExpect(status().isCreated());

        var captor = ArgumentCaptor.forClass(Evidence.class);
        verify(evidenceService).createEvidence(captor.capture());
        var sent = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(sent.getFileName()).isEqualTo("new");
        org.assertj.core.api.Assertions.assertThat(sent.getFileType()).isEqualTo("image/png");
    }


}
