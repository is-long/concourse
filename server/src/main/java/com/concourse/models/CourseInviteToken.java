package com.concourse.models;

import com.concourse.tools.StringTools;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class CourseInviteToken {

    @Id
    private String inviteId = StringTools.generateID(32) + StringTools.generateID(32);
    private String email;
    private String courseId;
    private String role;
}
