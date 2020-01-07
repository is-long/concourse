package com.concourse.repository;

import com.concourse.models.CourseInviteToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseInviteTokenRepository extends CrudRepository<CourseInviteToken, String> {

}
