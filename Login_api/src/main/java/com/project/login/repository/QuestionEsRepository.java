package com.project.login.repository;


import com.project.login.model.entity.QuestionEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionEsRepository extends ElasticsearchRepository<QuestionEntity, String> {

}

