package com.booknest.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;
    private String provider;
    private Long mobile;
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters and setters
}
