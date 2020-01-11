package com.concourse.models.posts;

import com.concourse.models.users.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class QuestionRootAnswerReply extends Post {
    private String questionRootAnswerId;

    public QuestionRootAnswerReply(){
        super();
    }

    public QuestionRootAnswerReply(String courseId, String content, User author, String questionRootAnswerId){
        super(courseId, content, author);
        setQuestionRootAnswerId(questionRootAnswerId);
    }
}
