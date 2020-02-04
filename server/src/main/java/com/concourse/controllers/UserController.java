package com.concourse.controllers;

import com.concourse.models.tokens.ConfirmationToken;
import com.concourse.models.Session;
import com.concourse.models.users.Instructor;
import com.concourse.models.users.Student;
import com.concourse.models.users.User;
import com.concourse.repository.*;
import com.concourse.tools.EmailServices;
import com.concourse.tools.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowCredentials = "true", allowedHeaders = "*")
@RequestMapping("user")
public class UserController {

    @Autowired
    private EmailServices emailServices;

    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private InstructorRepository instructorRepository;
    private StudentRepository studentRepository;
    private SessionRepository sessionRepository;
    private EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private PostRepository postRepository;

    public UserController(UserRepository userRepository, CourseRepository courseRepository, InstructorRepository instructorRepository, StudentRepository studentRepository, SessionRepository sessionRepository, EmailConfirmationTokenRepository emailConfirmationTokenRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
        this.postRepository = postRepository;
    }

    //===================================================
    // ACCESS USER; TODO BLOCK ACCESS
    //===================================================

    @PostMapping("self")
    public User getSelf(@RequestBody Session session) {
        if (session == null || session.getSessionId() == null || session.getEmail() == null) {
            log.info("Failed to check instructor role: Session has nulls");
            return null;
        }
        Optional<Session> optionalSession = sessionRepository.findById(session.getSessionId());
        if (!optionalSession.isPresent()) {
            log.info("Failed to check instructor role: Session does not exist");
            return null;
        }
        Optional<User> optionalUser = userRepository.findById(optionalSession.get().getEmail());
        if (optionalUser.isPresent()) {
            log.info("RETURNING SELF " + optionalUser.get());
            return optionalUser.get();
        }
        log.info("User does not exist?");
        return null;
    }

    @PostMapping("registration/check")
    public boolean checkRegistration(@RequestBody User user) {
        log.info("Checking registration for: " + user);
        //validate
        if (user == null || user.getEmail() == null) {
            log.info("Failed to check registration: Invalid user.");
            return false;
        }

        //validate email
        try {
            new InternetAddress(user.getEmail()).validate();
        } catch (AddressException e) {
            log.info("Failed to check registration: Invalid email");
            return false;
        }

        Optional<Instructor> optionalInstructor = instructorRepository.findById(user.getEmail());
        Optional<Student> optionalStudent = studentRepository.findById(user.getEmail());

        if (!optionalInstructor.isPresent() && !optionalStudent.isPresent()) {
            log.info("User is NOT registered.");
            return false;
        }
        if (optionalInstructor.isPresent() && !optionalInstructor.get().isEmailConfirmed()) {
            log.info("User is NOT registered. Pending confirmation.");

            //resend confirmation email
            ConfirmationToken token =
                    emailConfirmationTokenRepository.findConfirmationTokenByEmail(optionalInstructor.get().getEmail());
            emailServices.sendConfirmationToken(token.getCode(), optionalInstructor.get().getEmail());
            return false;
        }
        if (optionalStudent.isPresent() && !optionalStudent.get().isEmailConfirmed()) {
            log.info("User is NOT registered. Pending confirmation.");

            //resend confirmation email
            ConfirmationToken token =
                    emailConfirmationTokenRepository.findConfirmationTokenByEmail(optionalStudent.get().getEmail());
            emailServices.sendConfirmationToken(token.getCode(), optionalStudent.get().getEmail());
            return false;
        }
        log.info("User IS registered");
        return true;
    }

    //===================================================
    // NEW USER
    //===================================================

    @GetMapping("registration/confirm/{confirmationId}")
    public boolean confirmEmail(@PathVariable("confirmationId") String confirmationId) {
        Optional<ConfirmationToken> token = emailConfirmationTokenRepository.findById(confirmationId);

        if (token.isPresent()) {
            Optional<Student> optionalStudent = studentRepository.findById(token.get().getEmail());
            Optional<Instructor> optionalInstructor = instructorRepository.findById(token.get().getEmail());
            if (optionalStudent.isPresent()) {
                Student student = optionalStudent.get();
                student.setEmailConfirmed(true);
                studentRepository.save(student);
                log.info("STUDENT EMAIL CONFIRMED: " + student);
                emailConfirmationTokenRepository.delete(token.get());
                return true;
            }
            if (optionalInstructor.isPresent()) {
                Instructor instructor = optionalInstructor.get();
                instructor.setEmailConfirmed(true);
                instructorRepository.save(instructor);
                log.info("INSTRUCTOR EMAIL CONFIRMED: " + instructor);
                emailConfirmationTokenRepository.delete(token.get());
                return true;
            }
            log.info("Email confirmation failed: Student/instructor not found");
            return false;
        }
        log.info("Email confirmation failed: Invalid token");
        return false;
    }

    @PostMapping("registration/new/instructor")
    public Instructor addInstructor(@RequestBody Instructor instructor) {
        log.info("TRYING TO ADD INSTRUCTOR: " + instructor);
        log.info("TRYING TO ADD USER : " + ((User) instructor).getEmail());
        if (!checkRegistration(instructor)) {
            ConfirmationToken token =
                    emailConfirmationTokenRepository.findConfirmationTokenByEmail(instructor.getEmail());
            if (token == null) {
                token = new ConfirmationToken();
                token.setCode(StringTools.generateID(32) + StringTools.generateID(32));
                token.setEmail(instructor.getEmail());
            }
            emailConfirmationTokenRepository.save(token);
            emailServices.sendConfirmationToken(token.getCode(), instructor.getEmail());

            this.instructorRepository.save(instructor);
            log.info("SAVED INSTRUCTOR: " + instructor);
            return instructor;
        }
        return null;
    }

    @PostMapping("registration/new/student")
    public Student addStudent(@RequestBody Student student) {
        log.info("TRYING TO ADD STUDENT: " + student);
        log.info("TRYING TO ADD USER : " + (User) student);
        if (!checkRegistration(student)) {
            ConfirmationToken token =
                    emailConfirmationTokenRepository.findConfirmationTokenByEmail(student.getEmail());
            if (token == null) {
                token = new ConfirmationToken();
                token.setCode(StringTools.generateID(32) + StringTools.generateID(32));
                token.setEmail(student.getEmail());
            }
            emailConfirmationTokenRepository.save(token);
            emailServices.sendConfirmationToken(token.getCode(), student.getEmail());
            this.studentRepository.save(student);
            log.info("SAVED STUDENT: " + student);
            return student;
        }
        return null;
    }
}
