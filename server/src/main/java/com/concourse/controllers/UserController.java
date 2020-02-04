package com.concourse.controllers;

import com.concourse.models.Course;
import com.concourse.models.tokens.ConfirmationToken;
import com.concourse.models.Session;
import com.concourse.models.tokens.CourseInviteToken;
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
import java.util.HashMap;
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
    private SessionController sessionController;
    private CourseInviteTokenRepository courseInviteTokenRepository;


    public UserController(UserRepository userRepository, CourseRepository courseRepository, InstructorRepository instructorRepository, StudentRepository studentRepository, SessionRepository sessionRepository, EmailConfirmationTokenRepository emailConfirmationTokenRepository, PostRepository postRepository, SessionController sessionController, CourseInviteTokenRepository courseInviteTokenRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
        this.postRepository = postRepository;
        this.sessionController = sessionController;
        this.courseInviteTokenRepository = courseInviteTokenRepository;
    }

    //===================================================
    // ACCESS USER;
    //===================================================

    /**
     * Return user's own object, if the session belongs to the user
     * @param session user's session
     * @return the user's own object, if the request is authorized
     */
    @PostMapping("self")
    public User getSelf(@RequestBody Session session) {
        if (!sessionController.validate(session)) {
            log.info("Failed to get user: Invalid session");
            return null;
        }
        User user = userRepository.findById(session.getEmail()).get();
        log.info("RETURNING SELF " + user);
        return user;
    }

    /**
     * Check if user is registered AND has verified the email
     * @param user the user to be checked
     * @return true if user is registered, false otherwise
     */
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

    /**
     * Verify user's email and complete registration
     *
     * @param confirmationId the id sent to user's email in the confirmation link
     * @return true if user's email is confirmed, false otherwise
     */
    @GetMapping("registration/confirm/{confirmationId}")
    public boolean confirmEmail(@PathVariable("confirmationId") String confirmationId) {
        Optional<ConfirmationToken> optionalConfirmationToken = emailConfirmationTokenRepository.findById(confirmationId);

        if (optionalConfirmationToken.isPresent()) {
            ConfirmationToken token = optionalConfirmationToken.get();

            //check if student or instructor account
            Student student = studentRepository.getStudentElseNull(token.getEmail());
            if (student != null) {
                student.setEmailConfirmed(true);
                studentRepository.save(student);
                log.info("STUDENT EMAIL CONFIRMED: " + student);
                emailConfirmationTokenRepository.delete(token);


                //Send invite email to mock course for new student
                Course mockCourse = courseRepository.getMockCourse();
                HashMap<String, CourseInviteToken> emailTokenMap = new HashMap<>();
                CourseInviteToken inviteToken = new CourseInviteToken(mockCourse.getId(), "STUDENT", student.getEmail());
                courseInviteTokenRepository.save(inviteToken);
                emailTokenMap.put(student.getEmail(), inviteToken);
                emailServices.sendCourseInviteToken(emailTokenMap, mockCourse.getName());
                log.info("INVITATION TO MOCK COURSE SENT: " + student);
                return true;
            }

            Instructor instructor = instructorRepository.getInstructorElseNull(token.getEmail());
            if (instructor != null) {
                instructor.setEmailConfirmed(true);
                instructorRepository.save(instructor);
                log.info("INSTRUCTOR EMAIL CONFIRMED: " + instructor);
                emailConfirmationTokenRepository.delete(token);
                return true;
            }

            log.info("Email confirmation failed: Student/instructor not found");
            return false;
        }
        log.info("Email confirmation failed: Invalid token");
        return false;
    }

    /**
     * Add a new user of type instructor
     *
     * @param instructor new instructor to be added
     * @return the instructor, if successfully added, otherwise null
     */
    @PostMapping("registration/new/instructor")
    public Instructor addInstructor(@RequestBody Instructor instructor) {
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

        log.info("Failed to save instructor: " + instructor);
        return null;
    }

    /**
     * Add a new user of type student
     *
     * @param student new student to be added
     * @return the student, if successfully added, otherwise null
     */
    @PostMapping("registration/new/student")
    public Student addStudent(@RequestBody Student student) {
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

        log.info("Failed to save student: " + student);
        return null;
    }
}
