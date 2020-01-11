package com.concourse.models.posts;

import com.concourse.models.users.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class QuestionRootAnswer extends Post {
    private String questionRootId;

    @OneToMany
    private List<QuestionRootAnswerReply> questionRootAnswerReplyList = new ArrayList<>();




    public QuestionRootAnswer(){
        super();
    }

    public QuestionRootAnswer(String courseId, String content, User author, String questionRootId){
        super(courseId, content, author);
        setQuestionRootId(questionRootId);
    }

    public List<QuestionRootAnswerReply> addQuestionRootAnswerReply(QuestionRootAnswerReply questionRootAnswerReply){
        this.questionRootAnswerReplyList.add(questionRootAnswerReply);
        return this.questionRootAnswerReplyList;
    }

    public List<QuestionRootAnswerReply> removeQuestionRootAnswerReply(QuestionRootAnswerReply questionRootAnswerReply){
        this.questionRootAnswerReplyList.remove(questionRootAnswerReply);
        return this.questionRootAnswerReplyList;
    }
}
