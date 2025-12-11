package com.project.login.model.dto.userbehavior;

import com.project.login.model.dto.userbehavior.BehaviorType;
import lombok.Data;

@Data
public class UserBehaviorDTO {
    private Long userId;
    private BehaviorType behaviorType;
    private Long targetId;
    private Long timestamp;
}
