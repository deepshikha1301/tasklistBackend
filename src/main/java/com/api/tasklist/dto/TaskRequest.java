package com.api.tasklist.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskRequest(@NotBlank(message="loginId cannot be blank") String loginId,
                          @NotBlank(message="task cannot be blank") String taskName) {}

