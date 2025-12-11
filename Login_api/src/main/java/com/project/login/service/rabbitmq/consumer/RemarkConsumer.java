package com.project.login.service.rabbitmq.consumer;

import com.project.login.model.dataobject.RemarkCountDO;
import com.project.login.model.dataobject.RemarkLikeByUsersDO;
import com.project.login.repository.RemarkLikeByUsersRepository;
import com.project.login.repository.RemarkLikeCountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemarkConsumer {
    private final RemarkLikeCountRepository remarkLikeCountRepository;
    private final RemarkLikeByUsersRepository remarkLikeByUsersRepository;
    @RabbitListener(queues = "remarkLikeCount.redis.queue")
    public void handleLikeCountMessage(Map<String, Object> msg) {
        String remarkId = (String) msg.get("remarkId");
        Long likeCount = Long.parseLong(msg.get("likeCount").toString());
        if(likeCount!=null){
            log.info("get likeCount"+likeCount.toString());
        }
        if(remarkId==null){
            return;
        }
        // 点赞数量写入 MongoDB
        remarkLikeCountRepository.findById(remarkId).ifPresentOrElse(
                remarkCountDO -> {
                    remarkCountDO.setRemarkLikeCount(likeCount);
                    remarkLikeCountRepository.save(remarkCountDO);
                },
                () -> {
                    RemarkCountDO remarkCountDO = new RemarkCountDO();
                    remarkCountDO.setRemarkId(remarkId);
                    remarkCountDO.setRemarkLikeCount(likeCount);
                    remarkLikeCountRepository.save(remarkCountDO);
                }
        );
    }
    @RabbitListener(queues = "remarkLikeUsers.redis.queue")
    public void handleLikeUsersMessage(Map<String, Object> msg) {
        String remarkId = (String) msg.get("remarkId");
        Object usersObj = msg.get("users");
        Set<Long> userSet=new HashSet<>();
        log.info("usersObj = {}", usersObj);
        log.info("usersObj class = {}", usersObj == null ? "null" : usersObj.getClass().getName());

        if (usersObj instanceof Iterable<?>) {  // 包含 List / Set / LinkedHashSet / ArrayList
            for (Object o : (Iterable<?>) usersObj) {
                userSet.add(Long.parseLong(o.toString()));
            }
            log.info("users parsed as iterable: {}", userSet);
        } else {
            log.warn("users is not iterable, value = {}", usersObj);
        }
        // 点赞用户写入 MongoDB
        remarkLikeByUsersRepository.findById(remarkId).ifPresentOrElse(
                remarkLikeByUsersDO -> {
                    remarkLikeByUsersDO.setUserList(userSet);
                    remarkLikeByUsersRepository.save(remarkLikeByUsersDO);
                },
                () -> {
                    RemarkLikeByUsersDO remarkLikeByUsersDO = new RemarkLikeByUsersDO();
                    remarkLikeByUsersDO.setRemarkId(remarkId);
                    remarkLikeByUsersDO.setUserList(userSet);
                    remarkLikeByUsersRepository.save(remarkLikeByUsersDO);
                }
        );
    }
}
