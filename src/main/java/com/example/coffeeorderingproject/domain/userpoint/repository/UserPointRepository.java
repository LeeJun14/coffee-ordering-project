package com.example.coffeeorderingproject.domain.userpoint.repository;

import com.example.coffeeorderingproject.domain.userpoint.entity.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

    Optional<UserPoint> findByUser_UserId(Long userId);
}
