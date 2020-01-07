package com.concourse.models.posts;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class FollowupQuestion extends Post {
    private String questionRootId;

    @ElementCollection
    private List<String> followupAnswerIds;
}
