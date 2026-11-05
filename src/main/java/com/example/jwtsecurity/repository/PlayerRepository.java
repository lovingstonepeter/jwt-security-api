package com.example.jwtsecurity.repository;

import com.example.jwtsecurity.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
