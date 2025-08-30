package com.paralegal.paralegalApp.ControllerTest;

import com.paralegal.paralegalApp.Controller.CommentController;
import com.paralegal.paralegalApp.Exceptions.CommentNotFoundException;
import com.paralegal.paralegalApp.Model.Comment;
import com.paralegal.paralegalApp.Service.CommentService;
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
public class CommentControllerTest {

    @Mock
    private CommentService commentService;

    MockMvc mockMvc;
    @RestControllerAdvice
    static class ApiExceptionHandler{
        @ExceptionHandler(CommentNotFoundException.class)
        ResponseEntity<String> handle(CommentNotFoundException ex){
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }
    @BeforeEach
    void setup(){
        var controller = new CommentController(commentService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }
    @Test
    void createComment_returns201_andBody() throws Exception{
        var created = new Comment();
        created.setId(1L);
        created.setContent("First!");

        when(commentService.createComment(any(Comment.class))).thenReturn(created);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "First!"
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("First!"));

        var captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentService).createComment(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("First!");
    }
    @Test
    void createComment_invalidJson_returns400() throws Exception{
        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not-json}"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void getAllComment_returns200_andList() throws Exception{
        var c1 = Comment.builder().id(1L).content("A").createdAt(LocalDateTime.now()).build();
        var c2  = Comment.builder().id(2L).content("B").createdAt(LocalDateTime.now()).build();

        when(commentService.getAllComments()).thenReturn(List.of(c1,c2));

        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].content").value("A"))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(commentService).getAllComments();
    }
    @Test
    void getCommentById_found_returns200() throws Exception{
        var c = Comment.builder().id(10L).content("hello").build();

        when(commentService.getCommentById(10L)).thenReturn(Optional.of(c));

        mockMvc.perform(get("/api/comments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.content").value("hello"));

        verify(commentService).getCommentById(10L);
    }
    @Test
    void getCommentById_notFound_returns404() throws Exception{
        when(commentService.getCommentById(Mockito.eq(99L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/comments/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Comment Not Found by id: 99"));

        verify(commentService).getCommentById(99L);
    }

    @Test
    void updateComment_found_returns200_andBody() throws Exception{
        var saved = Comment.builder().id(5L).content("updated").build();

        when(commentService.updateComment(Mockito.eq(5L),any(Comment.class))).thenReturn(saved);

        mockMvc.perform(put("/api/comments/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
        {
        "content": "updated"
        }
        """
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.content").value("updated"));

        verify(commentService).updateComment(Mockito.eq(5L),any(Comment.class));
    }
    @Test
    void updateComment_notFound_returns404() throws Exception{
        when(commentService.updateComment(Mockito.eq(77L), any(Comment.class)))
                .thenThrow(new CommentNotFoundException("Comment Not Found"));

        mockMvc.perform(put("/api/comments/77")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Comment Not Found"));

        verify(commentService).updateComment(Mockito.eq(77L), any(Comment.class));
    }
    @Test
    void partiallyUpdatedComment_found_returns200_andBody() throws Exception{
        var patched = Comment.builder().id(7L).content("partially-updated").build();
        when(commentService.partiallyUpdateComment(Mockito.eq(7L),any(Map.class))).thenReturn(patched);

        mockMvc.perform(patch("/api/comments/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "content":"partially-updated"
                        }
                        """
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.content").value("partially-updated"));

        verify(commentService).partiallyUpdateComment(Mockito.eq(7L),any(Map.class));
    }
    @Test
    void partiallyUpdatedComment_notFound_returns404() throws Exception{
        when(commentService.partiallyUpdateComment(Mockito.eq(404L),any(Map.class)))
                .thenThrow(new CommentNotFoundException("Comment Not Found by id: 404"));

        mockMvc.perform(patch("/api/comments/404")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                  "content": "x"
                                }
                                """
                )
        )
                .andExpect(status().isNotFound())
                .andExpect(content().string("Comment Not Found by id: 404"));

        verify(commentService).partiallyUpdateComment(Mockito.eq(404L),any(Map.class));
    }
    @Test
    void deleteComment_returns204() throws Exception{
        doNothing().when(commentService).deleteComment(3L);

        mockMvc.perform(delete("/api/comments/3"))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(3L);
    }

}
