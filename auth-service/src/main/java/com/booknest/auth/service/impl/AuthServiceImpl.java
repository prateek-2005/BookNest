package com.booknest.auth.service.impl;

import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.security.JwtUtil;
import com.booknest.auth.service.AuthService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User register(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }
    public User authenticate(String email, String password) {
        List<User> users = userRepository.findAllByEmail(email);
        for (User user : users) {
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return user;
            }
        }
        return null;
    }


    public void logout(String token) {
        // JWT logout is usually handled client-side (discard token).
        // Optionally maintain a blacklist if needed.
    }

    public String refreshToken(String token) {
        if (jwtUtil.validateToken(token)) {
            String email = jwtUtil.getEmailFromToken(token);
            // Ideally fetch role from DB again
            User user = userRepository.findTopByEmailOrderByUserIdDesc(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return jwtUtil.generateToken(email, user.getRole());
        }
        throw new RuntimeException("Invalid token");
    }

    public void changePassword(int id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void resetPassword(String email, Long mobile, String newPassword) {
        User user = userRepository.findTopByEmailOrderByUserIdDesc(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        if (user.getMobile() == null || !user.getMobile().equals(mobile)) {
            throw new RuntimeException("Mobile number verification failed");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void changeMobile(int id, Long mobile) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setMobile(mobile);
        userRepository.save(user);
    }


    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
