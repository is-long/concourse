package com.concourse.repository;

import com.concourse.models.Course;
import com.concourse.models.posts.QuestionRoot;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends CrudRepository<Course, String> {


    default boolean saveQuestionRootToCourse(String courseId, QuestionRoot questionRoot){
        Optional<Course> optionalCourse = this.findById(courseId);
        if (!optionalCourse.isPresent()){
            return false;
        }
        Course course = optionalCourse.get();
        List<QuestionRoot> questionRootList = course.getQuestionRootList();
        questionRootList.add(questionRoot);
        course.setQuestionRootList(questionRootList);
        this.save(course);
        return true;
    }

}
