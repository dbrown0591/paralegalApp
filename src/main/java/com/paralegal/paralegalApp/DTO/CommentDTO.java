package com.paralegal.paralegalApp.DTO;


import java.time.LocalDateTime;

public record CommentDTO(Long id, String content, LocalDateTime createdAt) {}
