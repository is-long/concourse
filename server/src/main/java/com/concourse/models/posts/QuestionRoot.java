package com.concourse.models.posts;

import com.concourse.models.users.User;
import lombok.Builder;
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
public class QuestionRoot extends Post {
    private String title;
    private Integer viewCount = 0;
    private String folder;

    private boolean hasInstructorAnswer = false;

    @ElementCollection()
    private List<String> viewerIds = new ArrayList<>();

    @OneToMany
    private List<QuestionRootAnswer> questionRootAnswerList = new ArrayList<>();

    @OneToMany
    private List<FollowupQuestion> followupQuestionList = new ArrayList<>();

    public void setHasInstructorAnswer(){
        Boolean x = null;
        for (QuestionRootAnswer qra: questionRootAnswerList) {
            if (qra.getAuthorType().equals("INSTRUCTOR")){
                x = true;
                hasInstructorAnswer = true;
                break;
            }
        }
        if (x == null){
            hasInstructorAnswer = false;
        }
    }





    public QuestionRoot(){
        super();
    }

    public QuestionRoot(String courseId, String content, User author, String title) {
        super(courseId, content, author);
        setTitle(title);
    }

    public List<String> addViewerId(String viewerId) {
        if (!this.viewerIds.contains(viewerId)){
            this.viewerIds.add(viewerId);
            this.viewCount++;
        }
        return this.viewerIds;
    }

    public List<QuestionRootAnswer> addQuestionRootAnswer(QuestionRootAnswer questionRootAnswer) {
        this.questionRootAnswerList.add(questionRootAnswer);
        return this.questionRootAnswerList;
    }

    public List<QuestionRootAnswer> replaceQuestionRootAnswer(String oldQuestionRootAnswer, QuestionRootAnswer questionRootAnswer) {
        //remove old question root
        for (QuestionRootAnswer qra : questionRootAnswerList) {
            if (qra.getId().equals(oldQuestionRootAnswer)){
                removeQuestionRootAnswer(qra);
                break;
            }
        }
        //add new
        this.questionRootAnswerList.add(questionRootAnswer);
        return this.questionRootAnswerList;
    }

    public List<QuestionRootAnswer> removeQuestionRootAnswer(QuestionRootAnswer questionRootAnswer) {
        this.questionRootAnswerList.remove(questionRootAnswer);
        return this.questionRootAnswerList;
    }

    public List<FollowupQuestion> addFollowupQuestion(FollowupQuestion followupQuestion) {
        this.followupQuestionList.add(followupQuestion);
        return this.followupQuestionList;
    }

    public List<FollowupQuestion> replaceFollowupQuestion(String oldFollowupQuestion, FollowupQuestion followupQuestion) {
        //remove old followup question
        for (FollowupQuestion fq : followupQuestionList) {
            if (fq.getId().equals(oldFollowupQuestion)){
                removeFollowupQuestion(fq);
                break;
            }
        }
        //add new
        this.followupQuestionList.add(followupQuestion);
        return this.followupQuestionList;
    }

    public List<FollowupQuestion> removeFollowupQuestion(FollowupQuestion followupQuestion) {
        this.followupQuestionList.remove(followupQuestion);
        return this.followupQuestionList;
    }

}
