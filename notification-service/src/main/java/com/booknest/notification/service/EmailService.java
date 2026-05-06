package com.booknest.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RestTemplate restTemplate;

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void sendEmail(Long userId, String message) {
        try {
            String email = restTemplate.getForObject(
                "http://AUTH-SERVICE/auth/user/" + userId + "/email",
                String.class
            );
            if (email == null || email.isBlank() || mailSender == null) return;

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject("BookNest Notification");
            mail.setText(message);
            mailSender.send(mail);
        } catch (Exception e) {
            // log error and continue
        }
    }
}


