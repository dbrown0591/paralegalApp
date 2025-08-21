package com.paralegal.paralegalApp.Controller;

import com.paralegal.paralegalApp.DTO.CreateCommentRequest;
import com.paralegal.paralegalApp.Model.Comment;
import com.paralegal.paralegalApp.Service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/incidents/{incidentId}/comments")
@CrossOrigin("*")
@RequiredArgsConstructor
public class IncidentCommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<Comment>> list(@PathVariable Long incidentId) {
        return ResponseEntity.ok(commentService.getCommentsByIncidentID(incidentId));
    }

    @PostMapping
    public ResponseEntity<Comment> add(@PathVariable Long incidentId,
                                       @RequestBody CreateCommentRequest body) {
        Comment saved = commentService.createCommentForIncident(incidentId, body.content());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }
}

