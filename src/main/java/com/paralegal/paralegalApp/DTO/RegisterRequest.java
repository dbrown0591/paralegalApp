package com.paralegal.paralegalApp.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max =50) String userName,
        @Email @NotBlank String email,
        @NotBlank @Size(min=6) String password
) {}
