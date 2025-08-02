package com.paralegal.paralegalApp.Repository;

import com.paralegal.paralegalApp.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
