package com.project.login.service.rabbitmq.consumer;

import com.project.login.model.entity.NoteEntity;
import com.project.login.model.event.EsNoteEvent;
import com.project.login.model.event.NoteActionType;
import com.project.login.repository.NoteRepository;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Component
@RabbitListener(queues = "note.es.queue")
public class NoteEsConsumer {

    @Resource
    private NoteRepository esRepository;

    private final List<NoteEntity> batch = new ArrayList<>();
    private final int batchSize = 500;
    private volatile long lastFlush = System.currentTimeMillis();
    private final long flushIntervalMs = 2000;

    @RabbitHandler
    public void process(EsNoteEvent event) {

        switch (event.getAction()) {
            case NoteActionType.CREATE:
            case NoteActionType.UPDATE:
                NoteEntity entity = buildNoteEntity(event);
                synchronized (batch) {
                    batch.add(entity);
                    long now = System.currentTimeMillis();
                    if (batch.size() >= batchSize || (now - lastFlush) >= flushIntervalMs) {
                        esRepository.saveAll(new ArrayList<>(batch));
                        batch.clear();
                        lastFlush = now;
                    }
                }
                break;

            case NoteActionType.DELETE:
                esRepository.deleteById(event.getNoteId());
                break;

            default:
                System.out.println("Unknown action: " + event.getAction());
        }
    }

    // 确保部分更新时正确
    private NoteEntity buildNoteEntity(EsNoteEvent event) {
        Optional<NoteEntity> opt = esRepository.findById(event.getNoteId());
        NoteEntity entity = opt.orElse(new NoteEntity());

        // 设置ID（新建时需要）
        entity.setId(event.getNoteId());

        // 基本信息
        if (event.getTitle() != null) entity.setTitle(event.getTitle());
        if (event.getContentSummary() != null) entity.setContentSummary(event.getContentSummary());

        // 作者信息
        if (event.getAuthorName() != null) entity.setAuthorName(event.getAuthorName());

        // 统计信息
        if (event.getViewCount() != null) entity.setViewCount(event.getViewCount());
        if (event.getLikeCount() != null) entity.setLikeCount(event.getLikeCount());
        if (event.getFavoriteCount() != null) entity.setFavoriteCount(event.getFavoriteCount());
        if (event.getCommentCount() != null) entity.setCommentCount(event.getCommentCount());

        // 更新时间
        if (event.getUpdatedAt() != null) {
            entity.setUpdatedAt(event.getUpdatedAt());
        }

        return entity;
    }



}
