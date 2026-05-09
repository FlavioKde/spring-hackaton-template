package com.interhack.spring_hackaton_template.repository;

import com.interhack.spring_hackaton_template.model.User;
import com.interhack.spring_hackaton_template.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(UserRole role);
    boolean existsByUsername(String username);
}
