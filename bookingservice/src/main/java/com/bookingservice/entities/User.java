package com.bookingservice.entities;

import com.bookingservice.enums.AuthProvider;
import com.bookingservice.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;
    private String fullName;
    @Column(unique = true, nullable = false)
    private String email;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Column(columnDefinition = "TEXT")
    private String pictureUrl;
    private AuthProvider authProvider;
    private String password; // hash value
    private String username;
}
