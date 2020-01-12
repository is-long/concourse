package com.concourse.models.tokens;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Calendar;
import java.util.Random;

@Entity
@Data
public class LoginToken {
    @Id
    private String email;
    private String code = generateCode();
    private Long expiration;

    public void renew(){
        code = generateCode();
        expiration = generateExpiration();
    }

    public String generateCode(){
        return String.valueOf(new Random().nextInt((999999- 100000) + 1) + 100000);
    }

    public Long generateExpiration(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 30);
        return c.getTimeInMillis();
    }
}
