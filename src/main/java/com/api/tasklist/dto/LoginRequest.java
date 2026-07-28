package com.api.tasklist.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message="loginId cannot be blank") String loginId,
                           @NotBlank(message="password cannot be blank") String password) {}

