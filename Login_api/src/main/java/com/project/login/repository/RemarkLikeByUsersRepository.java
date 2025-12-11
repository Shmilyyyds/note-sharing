package com.project.login.repository;

import com.project.login.model.dataobject.RemarkLikeByUsersDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RemarkLikeByUsersRepository extends MongoRepository<RemarkLikeByUsersDO, String> {

}
