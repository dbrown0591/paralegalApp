package com.paralegal.paralegalApp.Repository;

import com.paralegal.paralegalApp.Model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
