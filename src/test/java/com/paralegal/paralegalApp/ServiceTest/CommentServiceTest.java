package com.paralegal.paralegalApp.ServiceTest;

import com.paralegal.paralegalApp.Exceptions.CommentNotFoundException;
import com.paralegal.paralegalApp.Model.Comment;
import com.paralegal.paralegalApp.Model.Incident;
import com.paralegal.paralegalApp.Repository.CommentRepository;
import com.paralegal.paralegalApp.Repository.IncidentRepository;
import com.paralegal.paralegalApp.Service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment existing;
    private Incident incident;
    @BeforeEach
    void setUp(){

        incident = Incident.builder()
                .id(11L)
                .build();
        existing = Comment.builder()
                .id(1L)
                .content("before")
                .createdAt(LocalDateTime.parse("2025-08-11T10:15:30"))
                .incident(incident)
                .build();
    }
    @Test
    void getAllComments_returnsListFromRepo(){
        Comment c2 = Comment.builder().id(2L).content("second").incident(incident).build();
        when(commentRepository.findAll()).thenReturn(List.of(existing,c2));

        var result = commentService.getAllComments();

        assertThat(result).containsExactly(existing,c2);
        verify(commentRepository).findAll();
    }

    @Test
    void getCommentById_found_returnsOptionalWithValue(){
        when(commentRepository.findById(1L)).thenReturn(Optional.of(existing));

        var result = commentService.getCommentById(1L);

        assertThat(result).contains(existing);
        verify(commentRepository).findById(1L);
    }
    @Test
    void getCommentById_notFound_returnsEmptyOptional(){
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        var result = commentService.getCommentById(99L);

        assertThat(result).isEmpty();
        verify(commentRepository).findById(99L);
    }

    @Test
    void getCommentsByIncidentID_returnsOrderedList(){
        Comment c2 = Comment.builder().id(3L).content("b").incident(incident).build();
        when(commentRepository.findByIncidentIdOrderByIdAsc(11L)).thenReturn(List.of(existing,c2));

        var result = commentService.getCommentsByIncidentID(11L);

        assertThat(result).containsExactly(existing,c2);
        verify(commentRepository).findByIncidentIdOrderByIdAsc(11L);
    }
    @Test
    void createComment_savesAndReturns(){
        when(commentRepository.save(any())).thenReturn(existing);

        var saved = commentService.createComment(existing);

        assertThat(saved).isSameAs(existing);
        verify(commentRepository).save(existing);
    }
    @Test
    void createCommentForIncident_incidentExists_linksAndSaves(){
        when(incidentRepository.findById(11L)).thenReturn(Optional.of(incident));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0, Comment.class);
            c.setId(100L);
            return c;
        });

        var created = commentService.createCommentForIncident(11L, "text here");

        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getContent()).isEqualTo("text here");
        assertThat(created.getIncident()).isSameAs(incident);
        verify(incidentRepository).findById(11L);
        verify(commentRepository).save(any(Comment.class));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getIncident()).isSameAs(incident);
        assertThat(captor.getValue().getContent()).isEqualTo("text here");
    }
    @Test
    void createCommentFroIncident_incidentMissing_throws404(){
        when(incidentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createCommentForIncident(404L, "x"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Incident 404 not found");

        verify(incidentRepository).findById(404L);
        verifyNoInteractions(commentRepository);
    }
    @Test
    void updateComment_found_setsIdAndSaves(){
        when(commentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Comment incoming = Comment.builder()
                .id(999L)
                .incident(incident)
                .content("changed")
                .build();

        var result = commentService.updateComment(1L, incoming);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("changed");

        verify(commentRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
    }
    @Test
    void updateComment_notFound_Throws(){
        when(commentRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(()-> commentService.updateComment(77L, new Comment()))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining("Comment Not Found");

        verify(commentRepository).findById(77L);
        verify(commentRepository, never()).save(any(Comment.class));
    }
    @Test
    void partiallyUpdateComment_updatesComment_andSaves(){
        when(commentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String,Object> update = java.util.Map.of(
                "content","after"
        );

        var result = commentService.partiallyUpdateComment(1L, update);

        verify(commentRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("after");
    }
    @Test
    void partiallyUpdateComment_MissingComment_throws(){
        when(commentRepository.findById(555L)).thenReturn(Optional.empty());
         assertThatThrownBy(() -> commentService.partiallyUpdateComment(555L, java.util.Map.of("content", "x")))
                 .isInstanceOf(CommentNotFoundException.class)
                 .hasMessageContaining("Comment Not Found by id: 555");

         verify(commentRepository).findById(555L);
         verify(commentRepository, never()).save(any(Comment.class));
    }
    @Test
    void partiallyUpdateComment_unknownField_throws(){
        var exist = Comment.builder().id(1L).content("hi").build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(exist));

        Map<String, Object> updates = Map.of("notARealField", "whatever");

        var ex = assertThrows(IllegalArgumentException.class,
                () -> commentService.partiallyUpdateComment(1L, updates));

        assertTrue(ex.getMessage().contains("Unknown field: notARealField"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteComment_delegatesToRepository(){
      commentService.deleteComment(9L);

      verify(commentRepository).deleteById(9L);
    }
}
