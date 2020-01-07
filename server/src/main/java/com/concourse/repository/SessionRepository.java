package com.concourse.repository;

import com.concourse.models.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SessionRepository extends CrudRepository<Session, String> {
}
