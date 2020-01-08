package com.concourse.repository;

import com.concourse.models.posts.QuestionRoot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRootRepository extends CrudRepository<QuestionRoot, String> {

    List<QuestionRoot> findAllByCourseId(String courseId);
}
