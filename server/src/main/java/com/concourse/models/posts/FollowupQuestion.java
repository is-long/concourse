package com.concourse.models.posts;

import com.concourse.models.users.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class FollowupQuestion extends Post {
    private String questionRootId;

    @OneToMany
    private List<FollowupAnswer> followupAnswerList = new ArrayList<>();

    public FollowupQuestion(){
        super();
    }

    public FollowupQuestion(String courseId, String content, User author, String questionRootId) {
        super(courseId, content, author);
        setQuestionRootId(questionRootId);
    }

    public List<FollowupAnswer> addFollowupAnswer(FollowupAnswer followupAnswer){
        this.followupAnswerList.add(followupAnswer);
        return this.followupAnswerList;
    }
    public List<FollowupAnswer> removeFollowupAnswer(FollowupAnswer followupAnswer){
        this.followupAnswerList.remove(followupAnswer);
        return this.followupAnswerList;
    }
}
