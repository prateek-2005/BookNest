package com.booknest.auth.dto;

public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String role;
    private Long mobile;

    // Getters
    public String getFullName() {
        return fullName;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getRole() {
        return role;
    }
    public Long getMobile() {
        return mobile;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public void setMobile(Long mobile) {
        this.mobile = mobile;
    }
}
