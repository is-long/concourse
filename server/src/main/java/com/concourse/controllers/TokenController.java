package com.concourse.controllers;

import com.concourse.models.Token;
import com.concourse.repository.TokenRepository;
import com.concourse.tools.EmailTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowCredentials = "true", allowedHeaders = "*")
@RequestMapping("token")
public class TokenController {

    @Autowired
    private TokenRepository tokenRepository;

    public TokenController(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Generate token. Returns true if generation is successful.
     *
     * @param token input token MUST have email address
     * @return Returns true if generation is successful.
     */
    @PostMapping("new")
    public boolean generate(@RequestBody Token token) {
        if (token.getEmail() == null || !EmailTools.isValidEmailAddress(token.getEmail())) {
            log.info("Can't generate Token: Email is invalid");
            return false;
        }

        token.renew();  //creates code & set expiration date = now + 30 minutes
        try {
            //if send email is successful
            if (EmailTools.sendCode(token.getCode(), new InternetAddress(token.getEmail()))){
                this.tokenRepository.save(token);
                log.info("Token generated: " + token);
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
     * @param token Must have EMAIL & CODE
     * @return true if token is valid, false otherwise
     */
    @PostMapping("validate")
    public boolean validate(@RequestBody Token token) {
        log.info("Token to validate: " + token);
        if (token.getEmail() == null || !EmailTools.isValidEmailAddress(token.getEmail())) {
            log.info("Invalid token: Email is invalid");
            return false;
        }
        if (token.getCode() == null || token.getCode().length() != 6) {
            log.info("Invalid token: Code is invalid");
            return false;
        }
        Optional<Token> t = tokenRepository.findById(token.getEmail());
        if (!t.isPresent()) {
            log.info("Invalid token: Email does not exist");
            return false;
        }
        if (t.get().getExpiration() <= new Date().getTime()){
            log.info("Invalid token: token expired");
            return false;
        }
        if (!t.get().getCode().equals(token.getCode())) {
            log.info("Invalid token: Code does not match");
            return false;
        }
        log.info("VALID TOKEN");
        tokenRepository.deleteById(token.getEmail()); //clear
        return true;
    }
}
