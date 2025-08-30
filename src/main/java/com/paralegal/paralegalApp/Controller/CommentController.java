package com.paralegal.paralegalApp.Controller;

import com.paralegal.paralegalApp.DTO.CreateCommentRequest;
import com.paralegal.paralegalApp.Exceptions.CommentNotFoundException;
import com.paralegal.paralegalApp.Model.Comment;
import com.paralegal.paralegalApp.Service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/comments")
@CrossOrigin("*")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment){
        Comment savedComment = commentService.createComment(comment);
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<Comment>> getAllComments(){
        List<Comment> comments = commentService.getAllComments();
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id){
        return commentService.getCommentById(id)
                .map(comment -> new ResponseEntity<>(comment, HttpStatus.OK))
                .orElseThrow(() -> new CommentNotFoundException("Comment Not Found by id: " + id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id, @RequestBody Comment updateComment){
        Comment comment =  commentService.updateComment(id, updateComment);
        return new ResponseEntity<>(comment, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Comment> partiallyUpdateComment(@PathVariable Long id, @RequestBody Map<String, Object> update){
        Comment comment = commentService.partiallyUpdateComment(id, update);
        return new ResponseEntity<>(comment, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id){
        commentService.deleteComment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
