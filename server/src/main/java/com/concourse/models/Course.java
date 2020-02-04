package com.concourse.models;

import com.concourse.models.posts.QuestionRoot;
import com.concourse.tools.StringTools;
import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Entity
public class Course {

    @Id
    private String id = StringTools.generateID(32);  //course id
    private String name; //course display name
    private String description;
    private String creatorInstructorId;

    @ElementCollection
    private List<String> instructorIds = new ArrayList<>();

    @ElementCollection
    private List<String> studentIds = new ArrayList<>();

    //question folders
    @ElementCollection
    private List<String> folders = new ArrayList<>();

    @OneToMany
    private List<QuestionRoot> questionRootList = new ArrayList<>();

    public List<QuestionRoot> addQuestionRoot(QuestionRoot questionRoot) {
        this.questionRootList.add(questionRoot);
        return this.questionRootList;
    }

    public List<QuestionRoot> removeQuestionRoot(QuestionRoot questionRoot) {
        this.questionRootList.remove(questionRoot);
        return this.questionRootList;
    }

    public List<QuestionRoot> replaceQuestionRoot(String oldQuestionRootId, QuestionRoot questionRoot) {
        //remove old question list
        for (QuestionRoot qr: this.questionRootList) {
            if (qr.getId().equals(oldQuestionRootId)){
                questionRootList.remove(qr);
                break;
            }
        }

        //add new
        questionRootList.add(questionRoot);
        return this.questionRootList;
    }

    public List<String> addInstructor(String instructorId) {
        this.instructorIds.add(instructorId);
        return this.instructorIds;
    }

    public List<String> removeInstructor(String instructorId) {
        this.instructorIds.remove(instructorId);
        return this.instructorIds;
    }

    public List<String> addStudent(String studentId) {
        this.studentIds.add(studentId);
        return this.studentIds;
    }

    public List<String> removeStudent(String studentId) {
        this.studentIds.remove(studentId);
        return this.studentIds;
    }

    public List<String> addFolders(List<String> newFolders) {
        for (String folderName: newFolders) {
            if (!this.folders.contains(folderName)){
                this.folders.add(folderName.trim());
            }
        }
        return this.folders;
    };
}
