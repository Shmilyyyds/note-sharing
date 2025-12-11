package com.project.login.model.event;

public record UserSearchEvent(
        Long userId,
        String keyword,
        long timestamp
) {}

