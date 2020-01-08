package com.concourse.models;

import com.concourse.models.posts.QuestionRoot;
import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Course {

    @Id
    private String id;  //course id
    private String name; //course display name
    private String description;
    private String creatorInstructorId;

    @ElementCollection
    private List<String> instructorIds = new ArrayList<>();

    @ElementCollection
    private List<String> studentIds  = new ArrayList<>();

//    @ElementCollection
//    private List<String> questionRootIds  = new ArrayList<>();

    @OneToMany
    private List<QuestionRoot> questionRootList = new ArrayList<>();
}
