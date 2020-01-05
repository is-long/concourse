package com.concourse.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Calendar;
import java.util.UUID;

@Data
@Entity
public class Session {

    @Id
    private String sessionId;
    private String email;
    private Long expiration;


    public void renew(){
        sessionId = generateSessionId();
        expiration = generateExpiration();
    }

    /**
     * Generate 64-digit sessionId
     * @return 64-digit sessionId
     */
    public String generateSessionId(){
        return UUID.randomUUID().toString().concat(UUID.randomUUID().toString()).replaceAll("-", "");
    }

    public Long generateExpiration(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 60); //good for an hour
        return c.getTimeInMillis();
    }
}
