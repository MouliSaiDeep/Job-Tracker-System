package com.jobtracker.ats.repository;

import com.jobtracker.ats.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{
    // This method tells Hibernate: "SELECT * FROM users WHERE email = ?"
    Optional<User> findByEmail(String email);
}
