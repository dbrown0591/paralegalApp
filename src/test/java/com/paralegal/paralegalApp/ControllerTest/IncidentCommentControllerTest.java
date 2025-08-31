package com.paralegal.paralegalApp.ControllerTest;

import com.paralegal.paralegalApp.Controller.IncidentCommentController;
import com.paralegal.paralegalApp.Model.Comment;
import com.paralegal.paralegalApp.Service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import java.util.List;


import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IncidentCommentControllerTest {
    @Mock
    private CommentService commentService;
    private MockMvc mockMvc;
    @BeforeEach
    void setup(){
        var controller = new IncidentCommentController(commentService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }
    @Test
    void list_returns200_andCommentsForIncident() throws Exception{
        var c1 = Comment.builder().id(1L).content("A").createdAt(LocalDateTime.now()).build();
        var c2 = Comment.builder().id(2L).content("B").createdAt(LocalDateTime.now()).build();

        when(commentService.getCommentsByIncidentID(Mockito.eq(5L))).thenReturn(List.of(c1,c2));

        mockMvc.perform(get("/api/incidents/5/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0]content").value("A"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].content").value("B"));

        verify(commentService).getCommentsByIncidentID(5L);
    }
    @Test
    void list_returns200_andEmptyArray_whenNoComments() throws Exception{
        when(commentService.getCommentsByIncidentID(Mockito.eq(9L))).thenReturn(List.of());

        mockMvc.perform(get("/api/incidents/9/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(commentService).getCommentsByIncidentID(9L);
    }
    @Test
    void add_returns201_setsLocationHeader_andBody() throws Exception{
        var saved = Comment.builder().id(123L).content("hello").build();

        when(commentService.createCommentForIncident(Mockito.eq(5L),Mockito.eq("hello"))).thenReturn(saved);

        mockMvc.perform(post("/api/incidents/5/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                             {
                             "content":"hello"
                             }                         
                             """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/incidents/5/comments/123"))
                .andExpect(jsonPath("$.id").value(123L))
                .andExpect(jsonPath("$.content").value("hello"));

        verify(commentService).createCommentForIncident(5L, "hello");
    }
    @Test
    void add_invalidJson_return400() throws Exception{
        mockMvc.perform(post("/api/incidents/7/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not-json}"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void add_incidentNotFound_returns404() throws Exception{
        when(commentService.createCommentForIncident(Mockito.eq(42L), any()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Incident 42 not found"));

        mockMvc.perform(post("/api/incidents/42/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                       { "content": "x" }
                                """))
                .andExpect(status().isNotFound());

        verify(commentService).createCommentForIncident(Mockito.eq(42L),Mockito.eq("x"));
    }

}
