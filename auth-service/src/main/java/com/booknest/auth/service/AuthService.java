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
}
