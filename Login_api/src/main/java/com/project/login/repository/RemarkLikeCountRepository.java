package com.project.login.repository;

import com.project.login.model.dataobject.RemarkCountDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RemarkLikeCountRepository extends MongoRepository<RemarkCountDO,String> {

}
