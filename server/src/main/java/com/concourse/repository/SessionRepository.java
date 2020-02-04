package com.concourse.repository;

import com.concourse.models.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SessionRepository extends CrudRepository<Session, String> {

    default Session getSessionElseNull(String sessionId){

        Optional<Session> optionalSession = findById(sessionId);
        return optionalSession.orElse(null);
    }
}
