package com.example.jwtsecurity.dto;

import com.example.jwtsecurity.entity.Role;
import com.example.jwtsecurity.entity.UserEntity;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public record UserResponse(Long id, String username, Set<String> roles) {
    public static UserResponse from(UserEntity user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(TreeSet::new));
        return new UserResponse(user.getId(), user.getUsername(), roleNames);
    }
}
