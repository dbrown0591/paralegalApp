package com.paralegal.paralegalApp.GlobalValidationHandlerTest;

import com.paralegal.paralegalApp.Controller.IncidentCommentController;
import com.paralegal.paralegalApp.ExceptionHandler.GlobalExceptionHandler;
import com.paralegal.paralegalApp.Service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@ExtendWith(MockitoExtension.class)
public class GlobalValidationHandlerCoverageTest {
    @Mock
    private CommentService commentService;

    private MockMvc mockMvc;
    @BeforeEach
    void setup(){
        mockMvc = MockMvcBuilders
                .standaloneSetup(new IncidentCommentController(commentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
    @Test
    void addComment_validationError_hitsGlobalHandler_returns400_andFieldMap() throws Exception{
        mockMvc.perform(post("/api/incidents/5/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                 { "content": "" }
                                 """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // your handler returns Map<String,String>: { "content": "content is required" }
                .andExpect(jsonPath("$.content").value("content is required"));

        // Service should NOT be called on validation failure
        verify(commentService, never()).createCommentForIncident(5L, "");
    }

    @Test
    void addComment_missingField_hitsGlobalHandler_returns400() throws Exception {
        // content missing -> null -> also violates @NotBlank
        mockMvc.perform(post("/api/incidents/{incidentId}/comments", 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content").exists());

        verify(commentService, never()).createCommentForIncident(9L, null);
    }

}
