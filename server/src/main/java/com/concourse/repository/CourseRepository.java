package com.concourse.repository;

import com.concourse.models.Course;
import com.concourse.models.posts.QuestionRoot;
import org.springframework.data.repository.CrudRepository;

import java.util.Arrays;
import java.util.Collections;
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

    default Course getCourseElseNull(String courseId){
        if (courseId == null) return null;
        Optional<Course> optionalCourse = findById(courseId);
        return optionalCourse.orElse(null);
    }

    default Course getMockCourse(){
        return findById("d902112eea4c49b68f3871b1c5340ffd").get();
    }

    default void createMockCourse(){
        Course course = new Course();
        course.setId("d902112eea4c49b68f3871b1c5340ffd");
        course.setName("Introduction to Software Engineering");
        course.setCreatorInstructorId("islong@seas.upenn.edu");
        course.setDescription("Introduction to Software Engineering");
        course.setInstructorIds(Collections.singletonList("islong@seas.upenn.edu"));
        course.setFolders(Arrays.asList("General", "Logistics", "Module 1", "Module 2", "Midterm Exam"));
        save(course);
    }

}
