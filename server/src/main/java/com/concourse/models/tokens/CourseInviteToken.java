package com.concourse.models.tokens;

import com.concourse.tools.StringTools;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@NoArgsConstructor
@Data
@Entity
public class CourseInviteToken {

    public CourseInviteToken(String courseId, String role, String email){
       this.courseId = courseId;
       this.role = role;
       this.email = email;
    }

    @Id
    private String inviteId = StringTools.generateID(32) + StringTools.generateID(32);
    private String email;
    private String courseId;
    private String role;
}
