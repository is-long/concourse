package com.concourse.models.users;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Student extends User {

    @ElementCollection
    private List<String> courseEnrolledIds = new ArrayList<>();

    public List<String> addCourseEnrolledIds(String courseId){
        this.courseEnrolledIds.add(courseId);
        return this.courseEnrolledIds;
    }
    public List<String> removeCourseEnrolledIds(String courseId){
        this.courseEnrolledIds.remove(courseId);
        return this.courseEnrolledIds;
    }
}
