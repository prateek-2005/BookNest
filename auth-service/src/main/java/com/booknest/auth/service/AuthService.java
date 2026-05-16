package com.booknest.auth.service;

import com.booknest.auth.entity.User;

public interface AuthService {
    User register(User user);
    User authenticate(String email, String password);   // login/authenticate
    void logout(String token);
    String refreshToken(String token);
    void changePassword(int id, String newPassword);
    void resetPassword(String email, Long mobile, String newPassword);
    void changeMobile(int id, Long mobile);
    boolean validateToken(String token);                // validate JWT
    void createSession(String token, String email);     // create Redis session
    
    // OTP Methods
    void sendOtp(String email);
    boolean verifyOtp(String email, String otp);
}
