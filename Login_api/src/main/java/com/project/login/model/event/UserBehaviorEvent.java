package com.project.login.model.event;

public record UserBehaviorEvent(
        Long userId,
        Long noteId,
        String tags,
        int weight,
        long timestamp
) {}

