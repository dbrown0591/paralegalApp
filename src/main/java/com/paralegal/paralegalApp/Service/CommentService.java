package com.paralegal.paralegalApp.Service;

import com.paralegal.paralegalApp.Exceptions.CommentNotFoundException;
import com.paralegal.paralegalApp.Model.Comment;
import com.paralegal.paralegalApp.Repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class CommentService {
    @Autowired
    private CommentRepository commentRepository; // TODO: Convert to constructor injection once finalized if not using multiple constuctors


    public List<Comment> getAllComments(){
        return commentRepository.findAll();
    }

    public Optional<Comment> getCommentById(Long id){
        return commentRepository.findById(id);
    }

    public Comment createComment(Comment comment){
        return commentRepository.save(comment);
    }

    public Comment updateComment(Long id, Comment comment){
       return commentRepository.findById(id)
               .map(existingComment -> {
                   comment.setID(id);
                   return commentRepository.save(comment);
               }).orElseThrow(()->new CommentNotFoundException("Comment Not Found"));
    }

    public void deleteComment(Long id){
        commentRepository.deleteById(id);
    }
}
