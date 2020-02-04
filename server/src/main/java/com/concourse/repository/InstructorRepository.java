package com.concourse.repository;

import com.concourse.models.users.Instructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends CrudRepository<Instructor, String> {

    default Instructor getInstructorElseNull(String instructorId){
        if (instructorId == null) return null;
        Optional<Instructor> optionalInstructor = findById(instructorId);
        return optionalInstructor.orElse(null);
    }
}
