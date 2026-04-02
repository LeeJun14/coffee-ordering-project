package com.example.coffeeorderingproject.domain.menu.repository;

import com.example.coffeeorderingproject.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
