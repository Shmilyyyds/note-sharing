package com.project.login.repository;

import com.project.login.model.dataobject.QuestionDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends MongoRepository<QuestionDO, String> {

    QuestionDO findByQuestionId(String questionId);

    boolean existsByQuestionId(String questionId);

    void deleteByQuestionId(String questionId);
}
