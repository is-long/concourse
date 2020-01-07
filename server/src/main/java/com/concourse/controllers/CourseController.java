package com.concourse.controllers;

import com.concourse.models.Course;
import com.concourse.models.CourseInviteToken;
import com.concourse.models.Session;
import com.concourse.models.posts.Post;
import com.concourse.models.users.Instructor;
import com.concourse.models.users.Student;
import com.concourse.models.users.User;
import com.concourse.repository.*;
import com.concourse.tools.EmailServices;
import com.concourse.tools.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("course")
@CrossOrigin(origins = "*", allowCredentials = "true", allowedHeaders = "*")
public class CourseController {

    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private SessionRepository sessionRepository;
    private PostRepository postRepository;
    private SessionController sessionController;
    private StudentRepository studentRepository;
    private InstructorRepository instructorRepository;
    private CourseInviteTokenRepository courseInviteTokenRepository;
    private EmailServices emailServices;

    public CourseController(UserRepository userRepository, CourseRepository courseRepository, SessionRepository sessionRepository, PostRepository postRepository,
                            SessionController sessionController, StudentRepository studentRepository, InstructorRepository instructorRepository, CourseInviteTokenRepository courseInviteTokenRepository, EmailServices emailServices) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
        this.postRepository = postRepository;
        this.sessionController = sessionController;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.courseInviteTokenRepository = courseInviteTokenRepository;
        this.emailServices = emailServices;
    }


    @GetMapping("{courseId}/join")
    public boolean joinCourse(@PathVariable("courseId") String courseId,
                              @RequestParam("sessionId") String sessionId,
                              @RequestParam("inviteId") String inviteId) {
        //quick session/inviteId check
        if (courseId.length() != 32 || sessionId == null || sessionId.length() != 64 || inviteId == null || inviteId.length() != 64) {
            log.info("Failed to join course: Invalid session/inviteId.");
            return false;
        }

        //check if the session is valid
        if (!sessionRepository.findById(sessionId).isPresent()) {
            log.info("Failed to join course: Session does not exist.");
            return false;
        }

        //check if the course exists
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent()) {
            log.info("Failed to join course: Course does not exist.");
            return false;
        }

        //check if the invite id exists
        Optional<CourseInviteToken> optionalCourseInviteToken = courseInviteTokenRepository.findById(inviteId);
        if (!optionalCourseInviteToken.isPresent()) {
            log.info("Failed to join course: InviteId does not exist.");
            return false;
        }

        CourseInviteToken courseInviteToken = optionalCourseInviteToken.get();
        Course course = optionalCourse.get();

        //check the role
        if (courseInviteToken.getRole().equals("INSTRUCTOR")){
            //add instructor to course
            List<String> instructorIds = course.getInstructorIds();
            instructorIds.add(courseInviteToken.getEmail());
            course.setInstructorIds(instructorIds);
            courseRepository.save(course);  //update
            log.info("ADDED INSTRUCTOR TO COURSE");

            //Bug source: what if user is registered as student instead?

            //add courseInstructed to instructor
            Instructor instructor = instructorRepository.findById(courseInviteToken.getEmail()).get();
            List<String> courseInstructedIds = instructor.getCourseInstructedIds();
            courseInstructedIds.add(courseId);
            instructor.setCourseInstructedIds(courseInstructedIds);
            instructorRepository.save(instructor);
            log.info("ADDED COURSE INSTRUCTED TO INSTRUCTOR");

            courseInviteTokenRepository.delete(courseInviteToken); //clear token
            return true;
        }
        if (courseInviteToken.getRole().equals("STUDENT")){
            //add instructor to course
            List<String> studentIds = course.getStudentIds();
            studentIds.add(courseInviteToken.getEmail());
            course.setInstructorIds(studentIds);
            courseRepository.save(course);  //update
            log.info("ADDED STUDENT TO COURSE");

            //Bug source: what if user is registered as instructor instead?

            //add courseEnrolled to student
            Student student = studentRepository.findById(courseInviteToken.getEmail()).get();
            List<String> courseEnrolledIds = student.getCourseEnrolledIds();
            courseEnrolledIds.add(courseId);
            student.setCourseEnrolledIds(courseEnrolledIds);
            studentRepository.save(student);
            log.info("ADDED COURSE ENROLLED TO STUDENT");

            courseInviteTokenRepository.delete(courseInviteToken); //clear token
            return true;
        }
        log.info("Failed to join course: Invalid role.");
        return false;
    }

    @GetMapping("{courseId}/checkmember/{sessionId}")
    public User checkMember(@PathVariable("courseId") String courseId, @PathVariable String sessionId) {
        Optional<Session> optionalSession = sessionRepository.findById(sessionId);
        if (!optionalSession.isPresent()) {
            log.info("Failed to check member: Invalid session");
            return null;
        }
        Session session = optionalSession.get();
        Course course = getCourse(courseId);
        if (course == null) {
            log.info("Failed to check member: Course does not exist.");
            return null;
        }
        if (course.getInstructorIds().contains(session.getEmail())) {
            log.info("VALID MEMBER: INSTRUCTOR.");
            return instructorRepository.findById(session.getEmail()).get();
        }
        if (course.getStudentIds().contains(session.getEmail())) {
            log.info("VALID MEMBER: STUDENT.");
            return studentRepository.findById(session.getEmail()).get();
        }
        log.info("NOT A MEMBER");
        return null;
    }

    @GetMapping("{courseId}")
    public Course getCourse(@PathVariable("courseId") String courseId) {
        return this.courseRepository.findById(courseId).orElse(null);
    }

    @GetMapping("all")
    public List<Course> getAllCourse() {
        return (List<Course>) this.courseRepository.findAll();
    }

    @PostMapping("new/{sessionId}")
    public Course addCourse(@RequestBody Course course, @PathVariable("sessionId") String sessionId) {
        log.info("Trying to create course: " + course);

        //Check course fields
        if (course == null) {
            log.info("Failed to create course: Supplied course is null.");
            return null;
        }

        if (course.getName() == null ||
                course.getDescription() == null ||
                course.getInstructorIds() == null || course.getStudentIds() == null ||
                course.getQuestionRootIds() == null) {
            log.info("Failed to create course: Arguments supplied contains null (other than courseId).");
            return null;
        }
        if (course.getName().length() == 0 || course.getDescription().length() == 0 ||
                course.getInstructorIds().size() == 0) {
            log.info("Failed to create course: Argument name, desc, or instructorIds is of length 0 or empty.");
            return null;
        }
        if (course.getStudentIds().size() != 0 || course.getQuestionRootIds().size() != 0) {
            log.info("Failed to create course: Argument studentIds or questionRootIds is not empty on course initialization.");
            return null;
        }

        if (course.getInstructorIds().size() != 1) {
            log.info("Failed to create course: Argument instructorIds is not 1.");
            return null;
        }

        Optional<Session> s = sessionRepository.findById(sessionId);
        if (!s.isPresent()) {
            log.info("Failed to create course: Session is invalid.");
            return null;
        }
        Optional<Instructor> instructorOptional = instructorRepository.findById(s.get().getEmail());
        if (!instructorOptional.isPresent()) {
            log.info("Failed to create course: User does not exist.");
            return null;
        }
        Instructor instructor = instructorOptional.get();

        course.setId(StringTools.generateID(32));

        List<String> courseInstructedIds = instructor.getCourseInstructedIds();
        courseInstructedIds.add(course.getId());
        instructor.setCourseInstructedIds(courseInstructedIds);
        userRepository.save(instructor);  //update instructor
        instructorRepository.save(instructor);
        log.info("COURSE INSTRUCTED ADDED TO : " + instructor);

        //AFTER COURSE CREATED, SEND INVITATION
        Map<String, CourseInviteToken> emailTokenMap = new HashMap<>();

        //to Instructor
        for (String email: course.getInstructorIds()) {
            //Create and save token
            CourseInviteToken token = new CourseInviteToken();
            token.setCourseId(course.getId());
            token.setRole("INSTRUCTOR");
            token.setEmail(email);
            token.setInviteId(StringTools.generateID(32) + StringTools.generateID(32));
            courseInviteTokenRepository.save(token);
            emailTokenMap.put(email, token);
        }

        //to Student
        for (String email: course.getStudentIds()) {
            //Create and save token
            CourseInviteToken token = new CourseInviteToken();
            token.setCourseId(course.getId());
            token.setRole("STUDENT");
            token.setEmail(email);
            token.setInviteId(StringTools.generateID(32) + StringTools.generateID(32));
            courseInviteTokenRepository.save(token);
            emailTokenMap.put(email, token);
        }

        //send email token
        emailServices.sendCourseInviteToken(emailTokenMap, course.getName());


        //edit the instructor, student, then save course
        List<String> instructorIds = new ArrayList<>();
        instructorIds.add(course.getCreatorInstructorId());    //only add the the creator instructor, since no creator accepted invitation yet
        course.setInstructorIds(instructorIds);
        course.setStudentIds(null);                            //since no student accepted invitation yet
        courseRepository.save(course);
        log.info("COURSE CREATED: " + course);
        return course;
    }


    @PostMapping("delete/{courseId}")
    public boolean deleteCourse(@PathVariable("courseId") String courseId, @RequestBody Session session) {
        if (!sessionController.validate(session)) {
            log.info("Failed to delete course : Invalid session");
            return false;
        }

        //Check if course exists
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent()) {
            log.info("Failed to delete course : Course does not exist");
            return false;
        }
        Course course = optionalCourse.get();
        //check user is the instructor of the course to be deleted
        if (!course.getInstructorIds().contains(session.getEmail())) {
            log.info("Failed to delete course : You're not the instructor for " + courseId);
            return false;
        }

        //remove course from all instructor, student, posts
        for (User u : userRepository.findAllById(course.getInstructorIds())) {
            Instructor instructor = (Instructor) u;
            List<String> courseInstructed = instructor.getCourseInstructedIds();
            courseInstructed.remove(courseId);
            instructor.setCourseInstructedIds(courseInstructed);
            userRepository.save(instructor);
            log.info("COURSE DELETED FROM INSTRUCTOR: " + instructor);
        }
        for (User u : userRepository.findAllById(course.getStudentIds())) {
            Student student = (Student) u;
            List<String> courseEnrolled = student.getCourseEnrolledIds();
            courseEnrolled.remove(courseId);
            student.setCourseEnrolledIds(courseEnrolled);
            userRepository.save(student);
            log.info("COURSE DELETED FROM STUDENT: " + student);
        }
        for (Post post : postRepository.findPostsByCourseId(courseId)) {
            postRepository.delete(post);
            log.info("POST DELETED: " + post);
        }

        courseRepository.delete(course);
        log.info("COURSE DELETED: " + course);
        return true;
    }
}
