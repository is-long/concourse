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
public class QuestionRootAnswer extends Post {
    private String questionRootId;

//    @ElementCollection
//    private List<String> questionRootAnswerReplyIds;

    @OneToMany
    private List<QuestionRootAnswerReply> questionRootAnswerReplyList;
}
