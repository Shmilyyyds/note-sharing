package com.project.login.controller;

import com.project.login.mapper.UserFavoriteNoteMapper;
import com.project.login.model.dataobject.UserFavoriteNoteDO;
import com.project.login.model.dto.userbehavior.BehaviorType;
import com.project.login.model.dto.userbehavior.UserBehaviorDTO;
import com.project.login.model.response.StandardResponse;
import com.project.login.model.vo.NoteStatsVO;
import com.project.login.service.notestats.NoteStatsService;
import com.project.login.service.notification.NotificationService;
import com.project.login.service.flink.userbahavior.UserBehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Tag(name = "Note Stats", description = "High-frequency note statistics")
@RestController
@RequestMapping("/api/v1/noting/note-stats")
@RequiredArgsConstructor
public class NoteStatsController {

    private final NoteStatsService noteStatsService;
    private final UserBehaviorService userBehaviorService;
    private final UserFavoriteNoteMapper userFavoriteNoteMapper;
    private final NotificationService notificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_LIKE_NOTE_KEY_PREFIX = "user_like_note:";
    private static final String USER_FAVORITE_NOTE_KEY_PREFIX = "user_favorite_note:";

    @Operation(summary = "Increment/Decrement a note statistic field")
    @PostMapping("/change")
    @Transactional
    public StandardResponse<NoteStatsVO> change(
            @Valid @RequestParam Long noteId,
            @RequestParam Long userId,
            @RequestParam String field,
            @RequestParam(defaultValue = "1") long delta) {

        // 幂等性检查：对于点赞和收藏，先检查用户是否已经操作过
        boolean shouldProceed = true;
        String fieldLower = field.toLowerCase();

        if ("likes".equals(fieldLower)) {
            shouldProceed = checkAndUpdateLikeStatus(noteId, userId, delta > 0);
        } else if ("favorites".equals(fieldLower)) {
            shouldProceed = checkAndUpdateFavoriteStatus(noteId, userId, delta > 0);
        }

        if (!shouldProceed) {
            // 操作无效（重复操作），返回当前统计信息
            NoteStatsVO vo = noteStatsService.getStats(noteId);
            return StandardResponse.success(vo);
        }

        // 更新笔记统计
        NoteStatsVO vo = noteStatsService.changeField(noteId, field, delta);

        // 构建用户行为记录
        UserBehaviorDTO dto = new UserBehaviorDTO();
        dto.setUserId(userId);
        dto.setTargetId(noteId);

        // 根据字段设置行为类型
        switch (fieldLower) {
            case "views":
                dto.setBehaviorType(BehaviorType.VIEW);
                break;
            case "likes":
                dto.setBehaviorType(BehaviorType.LIKE);
                // 点赞关系只存储在Redis中，不存储到数据库
                break;
            case "favorites":
                dto.setBehaviorType(BehaviorType.FAVORITE);
                // 处理收藏关系：如果 delta > 0 表示收藏，否则表示取消收藏
                if (delta > 0) {
                    // 收藏：插入或更新收藏关系
                    UserFavoriteNoteDO favorite = new UserFavoriteNoteDO();
                    favorite.setUserId(userId);
                    favorite.setNoteId(noteId);
                    userFavoriteNoteMapper.insert(favorite);
                } else {
                    // 取消收藏：删除收藏关系
                    userFavoriteNoteMapper.delete(userId, noteId);
                }
                break;
            case "comments":
                dto.setBehaviorType(BehaviorType.COMMENT);
                break;
            default:
                throw new IllegalArgumentException("Unsupported field: " + field);
        }

        // 记录用户行为
        userBehaviorService.recordBehavior(dto);

        // 创建相应通知（仅在增加时发送，避免取消点赞/收藏也发通知）
        if ("likes".equals(fieldLower) && delta > 0) {
            notificationService.createNoteLikeNotification(userId, noteId);
        } else if ("favorites".equals(fieldLower) && delta > 0) {
            notificationService.createNoteFavoriteNotification(userId, noteId);
        }

        return StandardResponse.success(vo);
    }

    /**
     * 检查并更新点赞状态，返回是否应该继续执行操作
     * 只使用Redis管理点赞状态，不依赖数据库
     * 使用Lua脚本确保原子性，防止并发问题
     * @return true表示可以继续操作，false表示重复操作
     */
    private boolean checkAndUpdateLikeStatus(Long noteId, Long userId, boolean isLike) {
        String redisKey = USER_LIKE_NOTE_KEY_PREFIX + noteId;
        String userIdStr = userId.toString();

        // 使用Lua脚本确保原子性：检查状态并更新
        String luaScript = 
            "local key = KEYS[1]\n" +
            "local userId = ARGV[1]\n" +
            "local isLike = ARGV[2]\n" +
            "local expireSeconds = ARGV[3]\n" +
            "local isMember = redis.call('SISMEMBER', key, userId)\n" +
            "if tonumber(isLike) == 1 then\n" +
            "    if isMember == 1 then\n" +
            "        return 0  -- 已经点赞过，返回0表示重复操作\n" +
            "    else\n" +
            "        redis.call('SADD', key, userId)\n" +
            "        redis.call('EXPIRE', key, expireSeconds)\n" +
            "        return 1  -- 成功点赞，返回1表示可以继续\n" +
            "    end\n" +
            "else\n" +
            "    if isMember == 0 then\n" +
            "        return 0  -- 没有点赞过，取消点赞无效\n" +
            "    else\n" +
            "        redis.call('SREM', key, userId)\n" +
            "        redis.call('EXPIRE', key, expireSeconds)\n" +
            "        return 1  -- 成功取消点赞，返回1表示可以继续\n" +
            "    end\n" +
            "end";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);
        Long result = redisTemplate.execute(
            script,
            Collections.singletonList(redisKey),
            userIdStr,
            isLike ? "1" : "0",
            String.valueOf(Duration.ofDays(30).getSeconds())
        );

        boolean shouldProceed = result != null && result == 1;
        if (!shouldProceed) {
            log.debug("User {} {} note {} failed (duplicate operation)", userId, isLike ? "like" : "unlike", noteId);
        }
        return shouldProceed;
    }

    /**
     * 检查并更新收藏状态，返回是否应该继续执行操作
     * 优先使用Redis，数据库作为持久化备份
     * 使用Lua脚本确保原子性，防止并发问题
     * @return true表示可以继续操作，false表示重复操作
     */
    private boolean checkAndUpdateFavoriteStatus(Long noteId, Long userId, boolean isFavorite) {
        String redisKey = USER_FAVORITE_NOTE_KEY_PREFIX + noteId;
        String userIdStr = userId.toString();

        // 如果Redis中没有数据，尝试从数据库加载（仅作为初始化）
        if (!redisTemplate.hasKey(redisKey)) {
            int count = userFavoriteNoteMapper.exists(userId, noteId);
            if (count > 0) {
                redisTemplate.opsForSet().add(redisKey, userIdStr);
            }
        }

        // 使用Lua脚本确保原子性：检查状态并更新
        String luaScript = 
            "local key = KEYS[1]\n" +
            "local userId = ARGV[1]\n" +
            "local isFavorite = ARGV[2]\n" +
            "local expireSeconds = ARGV[3]\n" +
            "local isMember = redis.call('SISMEMBER', key, userId)\n" +
            "if tonumber(isFavorite) == 1 then\n" +
            "    if isMember == 1 then\n" +
            "        return 0  -- 已经收藏过，返回0表示重复操作\n" +
            "    else\n" +
            "        redis.call('SADD', key, userId)\n" +
            "        redis.call('EXPIRE', key, expireSeconds)\n" +
            "        return 1  -- 成功收藏，返回1表示可以继续\n" +
            "    end\n" +
            "else\n" +
            "    if isMember == 0 then\n" +
            "        return 0  -- 没有收藏过，取消收藏无效\n" +
            "    else\n" +
            "        redis.call('SREM', key, userId)\n" +
            "        redis.call('EXPIRE', key, expireSeconds)\n" +
            "        return 1  -- 成功取消收藏，返回1表示可以继续\n" +
            "    end\n" +
            "end";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);
        Long result = redisTemplate.execute(
            script,
            Collections.singletonList(redisKey),
            userIdStr,
            isFavorite ? "1" : "0",
            String.valueOf(Duration.ofDays(30).getSeconds())
        );

        boolean shouldProceed = result != null && result == 1;
        if (!shouldProceed) {
            log.debug("User {} {} note {} failed (duplicate operation)", userId, isFavorite ? "favorite" : "unfavorite", noteId);
        }
        return shouldProceed;
    }


    @Operation(summary = "Get note statistics")
    @GetMapping("/{noteId}")
    public StandardResponse<NoteStatsVO> get(@PathVariable Long noteId) {
        NoteStatsVO vo = noteStatsService.getStats(noteId);
        return StandardResponse.success(vo);
    }
}
