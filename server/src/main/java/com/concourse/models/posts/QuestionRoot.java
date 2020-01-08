package com.concourse.models.posts;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class QuestionRoot extends Post {
    private Integer viewCount;

    @ElementCollection()
    private List<String> viewerIds;

    @OneToMany
    private List<QuestionRootAnswer> questionRootAnswerList;

    @OneToMany
    private List<FollowupQuestion> followupQuestionList;


//    @ElementCollection()
//    private List<String> questionRootAnswerIds;
//
//    @ElementCollection()
//    private List<String> followupQuestionIds;

}
