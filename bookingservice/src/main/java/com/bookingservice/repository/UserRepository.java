package com.bookingservice.repository;

import com.bookingservice.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsById(String id);

    Optional<User> findByEmail(String email);

    Optional<User> findFirstByUsername(String username);
}

