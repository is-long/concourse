package com.concourse.repository;

import com.concourse.models.users.Instructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstructorRepository extends CrudRepository<Instructor, String> {
}
