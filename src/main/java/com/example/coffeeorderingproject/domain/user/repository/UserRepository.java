package com.example.coffeeorderingproject.domain.user.repository;

import com.example.coffeeorderingproject.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
