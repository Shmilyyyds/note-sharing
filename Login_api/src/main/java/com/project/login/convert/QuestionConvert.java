package com.project.login.convert;

import com.project.login.model.dataobject.QuestionDO;
import com.project.login.model.vo.*;
import com.project.login.model.vo.qa.AnswerVO;
import com.project.login.model.vo.qa.CommentVO;
import com.project.login.model.vo.qa.QuestionVO;
import com.project.login.model.vo.qa.ReplyVO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class QuestionConvert {

    public QuestionVO toQuestionVO(QuestionDO doObj) {
        if (doObj == null) return null;

        QuestionVO vo = new QuestionVO();
        vo.setQuestionId(doObj.getQuestionId());
        vo.setAuthorId(doObj.getAuthorId());
        vo.setTitle(doObj.getTitle());
        vo.setContent(doObj.getContent());
        vo.setTags(doObj.getTags());
        vo.setCreatedAt(doObj.getCreatedAt());

        vo.setLikeCount(doObj.getLikes().size());
        vo.setFavoriteCount(doObj.getFavorites().size());

        vo.setAnswers(
                doObj.getAnswers().stream()
                        .map(this::toAnswerVO)
                        .collect(Collectors.toList())
        );

        return vo;
    }

    public AnswerVO toAnswerVO(QuestionDO.AnswerDO a) {
        AnswerVO vo = new AnswerVO();
        vo.setAnswerId(a.getAnswerId());
        vo.setAuthorId(a.getAuthorId());
        vo.setContent(a.getContent());
        vo.setCreatedAt(a.getCreatedAt());
        vo.setLikeCount(a.getLikes().size());

        vo.setComments(
                a.getComments().stream()
                        .map(this::toCommentVO)
                        .collect(Collectors.toList())
        );

        return vo;
    }

    public CommentVO toCommentVO(QuestionDO.CommentDO c) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(c.getCommentId());
        vo.setAuthorId(c.getAuthorId());
        vo.setContent(c.getContent());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setLikeCount(c.getLikes().size());

        vo.setReplies(
                c.getReplies().stream()
                        .map(this::toReplyVO)
                        .collect(Collectors.toList())
        );

        return vo;
    }

    public ReplyVO toReplyVO(QuestionDO.ReplyDO r) {
        ReplyVO vo = new ReplyVO();
        vo.setReplyId(r.getReplyId());
        vo.setAuthorId(r.getAuthorId());
        vo.setContent(r.getContent());
        vo.setCreatedAt(r.getCreatedAt());
        vo.setLikeCount(r.getLikes().size());

        return vo;
    }
}
