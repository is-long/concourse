package com.concourse.models.users;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Data
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User {

    @Id
    private String email;
    private String name;

    private String role;
    private boolean emailConfirmed;
}
