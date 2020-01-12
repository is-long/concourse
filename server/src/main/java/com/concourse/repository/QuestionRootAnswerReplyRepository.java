package com.concourse.repository;

import com.concourse.models.posts.QuestionRootAnswerReply;
import org.springframework.data.repository.CrudRepository;

public interface QuestionRootAnswerReplyRepository
        extends CrudRepository<QuestionRootAnswerReply, String> {
}
