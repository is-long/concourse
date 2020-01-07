package com.concourse.models.posts;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class FollowupAnswer extends Post {
    private String followupQuestionId;
}
