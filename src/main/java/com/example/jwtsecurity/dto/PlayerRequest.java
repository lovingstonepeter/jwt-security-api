package com.example.jwtsecurity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlayerRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(0) @Max(99) Integer jerseyNumber,
        @Size(max = 50) String position) {
}
