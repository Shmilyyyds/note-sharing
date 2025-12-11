package com.project.login.service.rabbitmq.consumer;

import com.project.login.model.entity.QuestionEntity;
import com.project.login.model.event.QuestionEvent;
import com.project.login.repository.QuestionEsRepository;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RabbitListener(queues = "question.es.queue")
public class QuestionEsConsumer {

    @Resource
    private QuestionEsRepository esRepository;

    @RabbitHandler
    public void process(QuestionEvent event) {

        switch (event.getType()) {
            case CREATE:
                esRepository.save(buildQuestionEntity(event));
                break;

            case DELETE:
                esRepository.deleteById(event.getQuestionId());
                break;

            default:
                System.out.println("Unknown event type: " + event.getType());
        }
    }

    // 构建或更新 QuestionEntity
    private QuestionEntity buildQuestionEntity(QuestionEvent event) {
        Optional<QuestionEntity> opt = esRepository.findById(event.getQuestionId());
        QuestionEntity entity = opt.orElse(new QuestionEntity());

        // 设置ID
        entity.setQuestionId(event.getQuestionId());

        // 设置基本信息
        if (event.getTitle() != null) entity.setTitle(event.getTitle());
        if (event.getContent() != null) entity.setContent(event.getContent());
        if (event.getTags() != null) entity.setTags(event.getTags());

        return entity;
    }
}

