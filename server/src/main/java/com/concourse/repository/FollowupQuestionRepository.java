package com.concourse.repository;

import com.concourse.models.posts.FollowupQuestion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowupQuestionRepository extends CrudRepository<FollowupQuestion, String> {
}
