package com.concourse.models.posts;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class QuestionRoot extends Post {
    private Integer viewCount;

    @ElementCollection()
    private List<String> viewerIds;

    @ElementCollection()
    private List<String> questionRootAnswerIds;

    @ElementCollection()
    private List<String> followupQuestionIds;
}
