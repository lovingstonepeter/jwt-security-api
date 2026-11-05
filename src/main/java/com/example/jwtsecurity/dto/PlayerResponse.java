package com.example.jwtsecurity.dto;

import com.example.jwtsecurity.entity.Player;

public record PlayerResponse(Long id, String name, Integer jerseyNumber, String position) {
    public static PlayerResponse from(Player p) {
        return new PlayerResponse(p.getId(), p.getName(), p.getJerseyNumber(), p.getPosition());
    }
}
