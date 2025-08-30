package com.paralegal.paralegalApp.Service;

import com.paralegal.paralegalApp.Exceptions.CommentNotFoundException;
import com.paralegal.paralegalApp.Model.Comment;
import com.paralegal.paralegalApp.Model.Incident;
import com.paralegal.paralegalApp.Repository.CommentRepository;
import com.paralegal.paralegalApp.Repository.IncidentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository; // TODO: Convert to constructor injection once finalized if not using multiple constuctors
    private final IncidentRepository incidentRepo;

    public List<Comment> getAllComments(){
        return commentRepository.findAll();
    }

    public Optional<Comment> getCommentById(Long id){
        return commentRepository.findById(id);
    }

    public List<Comment> getCommentsByIncidentID(Long incidentId){
        return commentRepository.findByIncidentIdOrderByIdAsc(incidentId);
    }

    public Comment createComment(Comment comment){
        return commentRepository.save(comment);
    }

    public Comment createCommentForIncident(Long incidentId, String text){
        Incident incident = incidentRepo.findById(incidentId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident " + incidentId + " not found"));

        Comment c = new Comment();
        c.setContent(text);
        c.setIncident(incident);
        return commentRepository.save(c);
    }

    public Comment updateComment(Long id, Comment comment){
       return commentRepository.findById(id)
               .map(existingComment -> {
                   comment.setId(id);
                   return commentRepository.save(comment);
               }).orElseThrow(()->new CommentNotFoundException("Comment Not Found"));
    }

    // Create partial
    @SuppressWarnings("ConstantConditions")
    public Comment partiallyUpdateComment(Long id, Map<String, Object> update){
        Comment comment = commentRepository.findById(id)
                .orElseThrow(()-> new CommentNotFoundException("Comment Not Found by id: " + id));

        update.forEach((key, value)-> {
            Field field = ReflectionUtils.findField(Comment.class,key);
            if(field == null){
                throw new IllegalArgumentException("Unknown field: " + key);
            }
            field.setAccessible(true);

            if("id".equals(key) || "createdAt".equals(key)) return;
            ReflectionUtils.setField(field, comment, value);

        });
        return commentRepository.save(comment);
    }

    public void deleteComment(Long id){
        commentRepository.deleteById(id);
    }
}
