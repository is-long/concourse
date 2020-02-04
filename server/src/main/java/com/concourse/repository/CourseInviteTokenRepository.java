package com.concourse.repository;

import com.concourse.models.tokens.CourseInviteToken;
import com.concourse.models.users.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseInviteTokenRepository extends CrudRepository<CourseInviteToken, String> {

    default CourseInviteToken getCourseInviteTokenElseNull(String inviteId){
        if (inviteId == null) return null;
        Optional<CourseInviteToken> optionalCourseInviteToken = findById(inviteId);
        return optionalCourseInviteToken.orElse(null);
    }
}
