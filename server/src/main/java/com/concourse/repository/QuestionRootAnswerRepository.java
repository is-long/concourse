package com.concourse.repository;

import com.concourse.models.posts.QuestionRootAnswer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRootAnswerRepository extends CrudRepository<QuestionRootAnswer, String> {
}
