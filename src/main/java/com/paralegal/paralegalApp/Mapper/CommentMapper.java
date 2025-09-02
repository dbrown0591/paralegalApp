package com.paralegal.paralegalApp.Mapper;
import com.paralegal.paralegalApp.DTO.CommentDTO;
import com.paralegal.paralegalApp.Model.Comment;

public final class CommentMapper {
    private CommentMapper() {}

    public static CommentDTO toDTO(Comment c) {
        return new CommentDTO(c.getId(), c.getContent(), c.getCreatedAt());
    }
}
