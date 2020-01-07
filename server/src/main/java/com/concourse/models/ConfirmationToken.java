package com.concourse.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Random;

@Entity
@Data
public class ConfirmationToken {
    @Id
    private String code = generateCode();
    private String email;

    public void renew(){
        code = generateCode();
    }

    public String generateCode(){
        return String.valueOf(new Random().nextInt((999999- 100000) + 1) + 100000);
    }

}
