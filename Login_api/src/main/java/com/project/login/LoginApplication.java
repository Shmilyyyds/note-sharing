package com.project.login;

import com.project.login.service.notestats.NoteStatsService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class LoginApplication {

    @Autowired
    private NoteStatsService noteStatsService;

    public static void main(String[] args) {
        SpringApplication.run(LoginApplication.class, args);
    }

    @PostConstruct
    public void preload() {
        noteStatsService.preloadRecent(100); // 启动延迟预热最近 100 条数据
    }
}
