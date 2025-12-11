package com.project.login.service.remark;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemarkScheduledTasks {

    private final RemarkService remarkService;


    @Scheduled(cron = "0 */5 * * * *")
    public void flushRedisLikeCountToMQ() {
        log.info("sending message 1");
        remarkService.flushLikeCountToMQ();
    }


    @Scheduled(cron = "0 */5 * * * *")
    public void flushRedisLikeUsersToMQ() {
        log.info("sending message 2");
        remarkService.flushLikeUsersToMQ();
    }
}

