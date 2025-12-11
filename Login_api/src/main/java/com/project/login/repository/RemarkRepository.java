package com.project.login.repository;

import com.project.login.model.dataobject.RemarkDO;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemarkRepository extends MongoRepository<RemarkDO, String> {
    @Query("{'noteId': ?0, 'isReply': false}")
    List<RemarkDO> findRemarksByNoteIdAndIsReplyFalse(Long noteId);

    @Query("{'parentId': ?0, 'isReply': true}")
    List<RemarkDO> findRemarksByParentIdAndIsReplyTrue(String parentId);
    List<RemarkDO> findByUserId(Long userId);



    void deleteByParentId(String parentId);      // 按 parentId 删除

}
