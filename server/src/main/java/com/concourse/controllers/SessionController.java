package com.concourse.controllers;

import com.concourse.models.Session;
import com.concourse.repository.SessionRepository;
import com.concourse.tools.EmailServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowCredentials = "true", allowedHeaders = "*")
@RequestMapping("session")
public class SessionController {

    private final SessionRepository sessionRepository;
    private final EmailServices emailServices;

    public SessionController(SessionRepository sessionRepository, EmailServices emailServices) {
        this.sessionRepository = sessionRepository;
        this.emailServices = emailServices;
    }

    /**
     * Generate new session
     * @param session session to be generated
     * @return the session to be generated
     */
    @PostMapping("new")
    public Session generate(@RequestBody Session session) {
        if (session.getEmail() == null || !emailServices.isValidEmailAddress(session.getEmail())) {
            log.info("Can't generate Session: Email is invalid");
            return null;
        }

        session.renew();  //creates code & set expiration date = now + 60 minutes
        this.sessionRepository.save(session);
        log.info("Session generated: " + session);
        return session;
    }

    /**
     * Validate session.
     * @param session session to be validated
     * @return true if the session is valid, false otherwise
     */
    @PostMapping("validate")
    public boolean validate(@RequestBody Session session){
        log.info("Session to validate: " + session);

        if (session == null){
           log.info("Invalid session: Session is null");
           return false;
        }
        if (session.getEmail() == null || !emailServices.isValidEmailAddress(session.getEmail())) {
            log.info("Invalid session: Email is invalid");
            return false;
        }
        if (session.getSessionId() == null || session.getSessionId().length() != 64) {
            log.info("Invalid session: SessionId is invalid");
            return false;
        }

        Optional<Session> s = sessionRepository.findById(session.getSessionId());
        if (!s.isPresent()){
            log.info("Invalid session: Session does not exist");
            return false;
        }
        if (s.get().getExpiration() <= new Date().getTime()){
            log.info("Invalid session: session expired");
            sessionRepository.deleteById(s.get().getSessionId());  //clear
            return false;
        }
        if (!s.get().getSessionId().equals(session.getSessionId())) {
            log.info("Invalid session: Code does not match");
            return false;
        }
        log.info("VALID SESSION");
        return true;
    }

    /**
     * Private helper method to validate sessionId
     * @param sessionId sessionId of the session to be validated
     * @return true if session is valid, false otherwise
     */
    public boolean validate(String sessionId){
        Session session = sessionRepository.getSessionElseNull(sessionId);
        if (session == null){
            log.info("Invalid session: Session does not exist");
            return false;
        }
        return validate(session);
    }

    /**
     * Remove session
     *
     * @param session session to be removed
     * @return true if the session is successfully removed, false otherwise
     */
    @PostMapping("purge")
    public boolean purge(@RequestBody Session session){
        if (session.getEmail() == null || !emailServices.isValidEmailAddress(session.getEmail())) {
            log.info("Invalid session: Email is invalid");
            return false;
        }
        if (session.getSessionId() == null || session.getSessionId().length() != 64) {
            log.info("Invalid session: SessionId is invalid");
            return false;
        }
        Optional<Session> s = sessionRepository.findById(session.getSessionId());
        if (!s.isPresent()){
            log.info("Invalid session: Session does not exist");
            return false;
        }
        sessionRepository.deleteById(session.getSessionId());
        return !sessionRepository.findById(session.getSessionId()).isPresent();
    }
}
