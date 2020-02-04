package com.concourse.controllers;

import com.concourse.models.Course;
import com.concourse.models.tokens.CourseInviteToken;
import com.concourse.models.Session;
import com.concourse.models.posts.*;
import com.concourse.models.users.Instructor;
import com.concourse.models.users.Student;
import com.concourse.models.users.User;
import com.concourse.repository.*;
import com.concourse.tools.EmailServices;
import com.concourse.tools.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
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

    public CourseController(UserRepository userRepository, CourseRepository courseRepository,
                            SessionRepository sessionRepository, PostRepository postRepository,
                            SessionController sessionController, StudentRepository studentRepository,
                            InstructorRepository instructorRepository,
                            CourseInviteTokenRepository courseInviteTokenRepository,
                            EmailServices emailServices) {
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


    //============================================================================
    //ACCESS COURSE
    //============================================================================

    @GetMapping("all")
    public List<Course> getAll() {
        return (List<Course>) courseRepository.findAll();
    }

    @PostMapping("{courseId}/get")
    public Course getCourse(@PathVariable("courseId") String courseId, @RequestBody Session session) {
        //validate session
        if (!sessionController.validate(session)) {
            log.info("Failed to get course: Invalid session.");
            return null;
        }

        //check course exists
        Optional<Course> optionalCourse = this.courseRepository.findById(courseId);  //null already checked by check member
        if (!optionalCourse.isPresent()) {
            log.info("Failed to get course: Course does not exist.");
            return null;
        }

        //check the user is involved in the course
        if (checkCourseMembership(courseId, session.getSessionId()) == null) {
            log.info("User is not involved in the course.");
            return null;
        }

        Course course = optionalCourse.get();
        log.info("FETCHING COURSE: " + course);
        return course;
    }

    @PostMapping("{courseId}/getfolders")
    public List<String> getFolders( @PathVariable("courseId") String courseId, @RequestBody Session session
    ) {
        //validate session
        if (!sessionController.validate(session)) {
            log.info("Failed to get course: Invalid session.");
            return null;
        }

        //check course exists
        Optional<Course> optionalCourse = this.courseRepository.findById(courseId);  //null already checked by check member
        if (!optionalCourse.isPresent()) {
            log.info("Failed to get course: Course does not exist.");
            return null;
        }

        //check user is involved in course, course exists
        if (checkCourseMembership(courseId, session.getSessionId()) == null) {
            log.info("Failed to add view: Invalid session or user is not in the course");
            return null;
        }

        List<String> folders = optionalCourse.get().getFolders();
        log.info("RETURNING FOLDERS: " + folders);
        return folders;
    }


    /*
       Before calling, make sure,
         1. Session exists
         2. Course exists
    */
    @GetMapping("{courseId}/checkmember/{sessionId}")
    public User checkCourseMembership(@PathVariable("courseId") String courseId, @PathVariable String sessionId) {
        String userEmail = sessionRepository.findById(sessionId).get().getEmail();
        Course course = courseRepository.findById(courseId).get();

        if (course.getInstructorIds().contains(userEmail)) {
            log.info("VALID MEMBER: INSTRUCTOR.");
            return instructorRepository.findById(userEmail).get();
        }

        if (course.getStudentIds().contains(userEmail)) {
            log.info("VALID MEMBER: STUDENT.");
            return studentRepository.findById(userEmail).get();
        }
        log.info("NOT A MEMBER");
        return null;
    }


    //============================================================================
    //MODIFY COURSE
    //============================================================================

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
        if (courseInviteToken.getRole().equals("INSTRUCTOR")) {
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
        } else if (courseInviteToken.getRole().equals("STUDENT")) {

            System.out.println("Course before: " + course);
            //add instructor to course
            List<String> studentIds = course.getStudentIds();
            studentIds.add(courseInviteToken.getEmail());
            course.setStudentIds(studentIds);
            System.out.println("Course after: " + course);
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

    @PostMapping("{courseId}/addfolders")
    public List<String> addFolders(
            @PathVariable("courseId") String courseId,
            @RequestParam("sessionId") String sessionId,
            @RequestBody List<String> newFolders
    ) {

        //check session, is involved
        log.info("Trying to addFolders : " + newFolders);
        if (courseId.length() != 32 || sessionId == null || sessionId.length() != 64 || newFolders == null) {
            log.info("Failed to addFolders: Invalid arguments");
            return null;
        }
        //check user is involved in course, course exists
        if (checkCourseMembership(courseId, sessionId) == null) {
            log.info("Failed to add folders: Invalid session or user is not in the course");
            return null;
        }
        Course course = courseRepository.findById(courseId).get();
        course.addFolders(newFolders);
        courseRepository.save(course);

        log.info("ADDED FOLDERS: " + course.getFolders());
        return course.getFolders();
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
                course.getQuestionRootList() == null) {
            log.info("Failed to create course: Arguments supplied contains null (other than courseId).");
            return null;
        }

        if (course.getName().length() == 0 || course.getDescription().length() == 0 ||
                course.getInstructorIds().size() == 0) {
            log.info("Failed to create course: Argument name, desc, or instructorIds is of length 0 or empty.");
            return null;
        }
        if (course.getQuestionRootList().size() != 0) {
            log.info("Failed to create course: Argument questionRootIds is non empty on course initialization.");
            return null;
        }

        //Make sure the ids don't overlap, i.e. instructor email can't also be in student's list
        Set<String> instructorIdsSet = new HashSet<>(course.getInstructorIds());
        Set<String> studentIdsSet = new HashSet<>(course.getStudentIds());
        instructorIdsSet.retainAll(studentIdsSet);
        if (!instructorIdsSet.isEmpty()) {
            log.info("Failed to create course: The instructor and student emails overlap.");
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
        //set course id
        course.setId(StringTools.generateID(32));

        //update instructor creator courseinstructed
        Instructor instructor = instructorOptional.get();
        List<String> courseInstructedIds = instructor.getCourseInstructedIds();
        courseInstructedIds.add(course.getId());
        instructor.setCourseInstructedIds(courseInstructedIds);
        userRepository.save(instructor);
        instructorRepository.save(instructor);  //update instructor, necessary?
        log.info("COURSE INSTRUCTED ADDED TO : " + instructor);

        //AFTER COURSE CREATED, SEND INVITATION
        Map<String, CourseInviteToken> emailTokenMap = new HashMap<>();

        //but remove self (the creator instructor) first
        List<String> instructorIdList = course.getInstructorIds();
        instructorIdList.remove(course.getCreatorInstructorId());
        course.setInstructorIds(instructorIdList);

        //to Instructor
        for (String email : course.getInstructorIds()) {
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
        for (String email : course.getStudentIds()) {
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
        course.setStudentIds(new ArrayList<>());                            //since no student accepted invitation yet
        courseRepository.save(course);
        log.info("COURSE CREATED: " + course);
        return course;
    }


    @PostMapping("{courseId}/delete/course")
    @Transactional
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

        //check if user is the creator of the course, the instructor of the course to be deleted
        if (!course.getCreatorInstructorId().equals(session.getEmail())) {
            log.info("Failed to delete course : You're not the creator of " + courseId);
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


    @PostMapping("{courseId}/invite")
    public boolean sendInvitation(
            @PathVariable("courseId") String courseId,
            @RequestBody List<String> emails,
            @RequestParam("sessionId") String sessionId,
            @RequestParam("role") String role
    ) {
        //Validate is an instructor of courseId
        Optional<Session> optionalSession = sessionRepository.findById(sessionId);
        if (!optionalSession.isPresent()) {
            log.info("Failed to send invitation: Invalid session");
            return false;
        }
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent()) {
            log.info("Failed to send invitation: Invalid course");
            return false;
        }
        Course course = optionalCourse.get();
        if (!course.getInstructorIds().contains(optionalSession.get().getEmail())) {
            log.info("Failed to send invitation: User not an instructor of the course");
            return false;
        }
        if (!role.equals("INSTRUCTOR") && !role.equals("STUDENT")) {
            log.info("Failed to send invitation: Invalid role");
            return false;
        }

        //Check if email in the list is valid
        HashMap<String, CourseInviteToken> emailTokenMap = new HashMap<>();
        for (String email : emails) {
            //if email is never registered before
            if (!course.getInstructorIds().contains(email) && !course.getStudentIds().contains(email)) {
                CourseInviteToken token = new CourseInviteToken();
                token.setCourseId(course.getId());
                token.setRole(role);
                token.setEmail(email);
                token.setInviteId(StringTools.generateID(32) + StringTools.generateID(32));
                courseInviteTokenRepository.save(token);
                emailTokenMap.put(email, token);
            }
        }

        //send email token
        emailServices.sendCourseInviteToken(emailTokenMap, course.getName());

        return true;
    }

    @PostMapping("{courseId}/delete/students")
    public boolean removeStudents(
            @PathVariable("courseId") String courseId,
            @RequestBody List<String> emails,
            @RequestParam("sessionId") String sessionId) {
        Optional<Session> optionalSession = sessionRepository.findById(sessionId);
        if (!optionalSession.isPresent()) {
            log.info("Failed to remove students: Invalid session");
            return false;
        }
        Session session = optionalSession.get();

        if (emails == null || emails.size() == 0) {
            log.info("Failed to remove students: No emails found");
            return false;
        }

        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent()) {
            log.info("Failed to remove students: Invalid course");
            return false;
        }
        Course course = optionalCourse.get();
        if (!course.getInstructorIds().contains(session.getEmail())) {
            log.info("Failed to remove students: Not an instructor");
            return false;
        }

        log.info("emails:" + emails);
        log.info("studentIds:" + emails);
        //remove students from course list
        List<String> studentIds = course.getStudentIds();
        studentIds.removeAll(emails);
        course.setStudentIds(studentIds);
        log.info("USER REMOVED FROM COURSE: " + course);
        courseRepository.save(course);

        //remove course from student's enrolled course list
        for (String email : emails) {
            Optional<Student> optionalStudent = studentRepository.findById(email);
            if (optionalStudent.isPresent()) {
                Student student = optionalStudent.get();
                student.removeCourseEnrolledIds(courseId);
                studentRepository.save(student);

                //delete content of the posts belonging to the students
                List<Post> studentPostsInCourse = postRepository.findPostsByAuthorUserIdAndCourseId(student.getEmail(), courseId);
                for (Post p : studentPostsInCourse) {
                    //p.setContent("[DELETED]");  //dont remove content
                    p.setAuthorName("[DELETED]");
                    p.setAuthorUserId("[DELETED]");
                    postRepository.save(p);
                }
            }
        }
        return true;
    }
}
