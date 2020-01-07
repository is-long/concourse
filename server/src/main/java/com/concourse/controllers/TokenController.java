package com.concourse.controllers;

import com.concourse.models.LoginToken;
import com.concourse.repository.InstructorRepository;
import com.concourse.repository.StudentRepository;
import com.concourse.repository.TokenRepository;
import com.concourse.repository.UserRepository;
import com.concourse.tools.EmailServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Date;
import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowCredentials = "true", allowedHeaders = "*")
@RequestMapping("token")
public class TokenController {

    @Autowired
    private EmailServices emailServices;

    @Autowired
    private TokenRepository tokenRepository;
    private UserRepository userRepository;
    private InstructorRepository instructorRepository;
    private StudentRepository studentRepository;

    public TokenController(TokenRepository tokenRepository, UserRepository userRepository, InstructorRepository instructorRepository, StudentRepository studentRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.instructorRepository = instructorRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Generate token. Returns true if generation is successful.
     *
     * @param loginToken input token MUST have email address
     * @return Returns true if generation is successful.
     */
    @PostMapping("new")
    public boolean generate(@RequestBody LoginToken loginToken) {
        if (loginToken.getEmail() == null || !emailServices.isValidEmailAddress(loginToken.getEmail())) {
            log.info("Can't generate Token: Email is invalid");
            return false;
        }

        loginToken.renew();  //creates code & set expiration date = now + 30 minutes
        try {
            //if send email is successful
            if (emailServices.sendCode(loginToken.getCode(), new InternetAddress(loginToken.getEmail()))){
                this.tokenRepository.save(loginToken);
                log.info("Token generated: " + loginToken);
                return true;
            };
        } catch (AddressException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Validate token.
     *
     * @param loginToken Must have EMAIL & CODE
     * @return true if token is valid, false otherwise
     */
    @PostMapping("validate")
    public boolean validate(@RequestBody LoginToken loginToken) {
        log.info("Token to validate: " + loginToken);
        if (loginToken.getEmail() == null || !emailServices.isValidEmailAddress(loginToken.getEmail())) {
            log.info("Invalid token: Email is invalid");
            return false;
        }
        if (loginToken.getCode() == null || loginToken.getCode().length() != 6) {
            log.info("Invalid token: Code is invalid");
            return false;
        }
        Optional<LoginToken> t = tokenRepository.findById(loginToken.getEmail());
        if (!t.isPresent()) {
            log.info("Invalid token: Email does not exist");
            return false;
        }
        if (t.get().getExpiration() <= new Date().getTime()){
            log.info("Invalid token: token expired");
            return false;
        }
        if (!t.get().getCode().equals(loginToken.getCode())) {
            log.info("Invalid token: Code does not match");
            return false;
        }
        log.info("VALID TOKEN");
        tokenRepository.deleteById(loginToken.getEmail()); //clear
        return true;
    }
}
