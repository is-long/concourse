package com.concourse.models.posts;

import com.concourse.models.users.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class FollowupAnswer extends Post {
    private String followupQuestionId;

    public FollowupAnswer(){
        super();
    }

    public FollowupAnswer(String courseId, String content, User author, String followupQuestionId) {
        super(courseId, content, author);
        setFollowupQuestionId(followupQuestionId);
    }
}
