package com.concourse.repository;

import com.concourse.models.users.Student;
import com.concourse.models.users.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends CrudRepository<Student, String> {
    default Student getStudentElseNull(String studentId){
        Optional<Student> optionalStudent = findById(studentId);
        return optionalStudent.orElse(null);
    }
}
