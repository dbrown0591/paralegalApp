package com.paralegal.paralegalApp.Repository;

import com.paralegal.paralegalApp.Model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByIncidentIdOrderByIdAsc(Long incidentId);
}
