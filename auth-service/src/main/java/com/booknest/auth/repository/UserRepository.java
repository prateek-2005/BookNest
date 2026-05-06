package com.booknest.auth.repository;

import com.booknest.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
	Optional<User> findTopByEmailOrderByUserIdDesc(String email);
	List<User> findAllByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByRole(String role);
    void deleteByUserId(int userId);
}
