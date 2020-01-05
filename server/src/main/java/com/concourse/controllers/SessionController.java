package com.concourse.controllers;

import com.concourse.models.Session;
import com.concourse.models.Token;
import com.concourse.repository.SessionRepository;
import com.concourse.tools.EmailTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowCredentials = "true", allowedHeaders = "*")
@RequestMapping("session")
public class SessionController {

    @Autowired
    private SessionRepository sessionRepository;

    public SessionController(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Generate new session
     * @param session
     * @return
     */
    @PostMapping("new")
    public Session generate(@RequestBody Session session) {
        if (session.getEmail() == null || !EmailTools.isValidEmailAddress(session.getEmail())) {
            log.info("Can't generate Session: Email is invalid");
            return null;
        }

        session.renew();  //creates code & set expiration date = now + 60 minutes
        this.sessionRepository.save(session);
        log.info("Session generated: " + session);
        return session;
    }

    @PostMapping("validate")
    public boolean validate(@RequestBody Session session){
        log.info("Session to validate: " + session);
        if (session.getEmail() == null || !EmailTools.isValidEmailAddress(session.getEmail())) {
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

    @PostMapping("purge")
    public boolean purge(@RequestBody Session session){
        if (session.getEmail() == null || !EmailTools.isValidEmailAddress(session.getEmail())) {
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
