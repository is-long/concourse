package com.concourse.models.users;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Instructor extends User {
    @ElementCollection
    private List<String> courseInstructedIds;

}
