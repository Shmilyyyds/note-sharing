package com.project.login.mapper;

import com.project.login.model.dataobject.UserFavoriteNoteDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserFavoriteNoteMapper {

    /** 插入收藏关系 */
    @Insert("""
        INSERT INTO user_favorite_note (user_id, note_id, favorite_time)
        VALUES (#{userId}, #{noteId}, NOW())
        ON DUPLICATE KEY UPDATE favorite_time = NOW()
    """)
    int insert(UserFavoriteNoteDO favorite);

    /** 删除收藏关系（取消收藏） */
    @Delete("""
        DELETE FROM user_favorite_note
        WHERE user_id = #{userId} AND note_id = #{noteId}
    """)
    int delete(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /** 判断是否已收藏 */
    @Select("""
        SELECT COUNT(1)
        FROM user_favorite_note
        WHERE user_id = #{userId} AND note_id = #{noteId}
    """)
    int exists(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /** 查询用户收藏的所有笔记ID */
    @Select("""
        SELECT note_id
        FROM user_favorite_note
        WHERE user_id = #{userId}
        ORDER BY favorite_time DESC
    """)
    List<Long> selectNoteIdsByUserId(@Param("userId") Long userId);
}
