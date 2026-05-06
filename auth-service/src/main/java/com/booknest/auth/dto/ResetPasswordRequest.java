package com.booknest.auth.dto;

public class ResetPasswordRequest {
    private String email;
    private Long mobile;
    private String newPassword;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getMobile() { return mobile; }
    public void setMobile(Long mobile) { this.mobile = mobile; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
