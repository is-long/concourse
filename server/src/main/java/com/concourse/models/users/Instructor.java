package com.concourse.models.users;

import lombok.*;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Instructor extends User {
    @ElementCollection
    private List<String> courseInstructedIds = new ArrayList<>();

    public List<String> addCourseInstructedIds(String courseId){
        this.courseInstructedIds.add(courseId);
        return this.courseInstructedIds;
    }
    public List<String> removeCourseInstructedIds(String courseId){
        this.courseInstructedIds.remove(courseId);
        return this.courseInstructedIds;
    }
}
