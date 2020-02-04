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

    public CourseController(UserRepository userRepository,
                            CourseRepository courseRepository,
                            SessionRepository sessionRepository,
                            PostRepository postRepository,
                            SessionController sessionController,
                            StudentRepository studentRepository,
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

        //Create mock course. All students will be enrolled in Mock Course
        courseRepository.createMockCourse();
    }


    //============================================================================
    //ACCESS COURSE
    //============================================================================

    /**
     * Returns the course if the user is involved in the course.
     *
     * @param courseId 32-bit courseId of the course
     * @param session user's sessionId
     * @return the Course of the corresponding courseId
     */
    @PostMapping("{courseId}/get")
    public Course getCourse(@PathVariable("courseId") String courseId, @RequestBody Session session) {
        //check the user is involved in the course
        if (checkCourseMembership(courseId, session.getSessionId()) == null) {
            log.info("Invalid session or User is not involved in the course.");
            return null;
        }

        Course course = courseRepository.getCourseElseNull(courseId);
        log.info("FETCHING COURSE: " + course);
        return course;
    }

    /**
     * Returns the folders of the courses
     *
     * @param courseId 32-bit courseId of the course
     * @param session user's sessionId
     * @return the folders of the course
     */
    @PostMapping("{courseId}/getfolders")
    public List<String> getFolders(@PathVariable("courseId") String courseId, @RequestBody Session session) {
        //check user is involved in course
        if (checkCourseMembership(courseId, session.getSessionId()) == null) {
            log.info("Failed to get folders: Invalid session or user is not involved in the course");
            return null;
        }
        List<String> folders = courseRepository.getCourseElseNull(courseId).getFolders();
        log.info("RETURNING FOLDERS: " + folders);
        return folders;
    }

    /**
     * Check if the user is involved as a student or instructor in the course.
     *
     * @param courseId  32-bit courseId of the course
     * @param sessionId 64-bit user's sessionId
     * @return the user the user is involved as a student or instructor in the course
     */
    @GetMapping("{courseId}/checkmember/{sessionId}")
    public User checkCourseMembership(@PathVariable("courseId") String courseId, @PathVariable String sessionId) {
        //check if the session is valid
        Session session = sessionRepository.getSessionElseNull(sessionId);
        if (session == null) {
            log.info("Failed to check membership: Session does not exist.");
            return null;
        }

        //check if the course exists
        Course course = courseRepository.getCourseElseNull(courseId);
        if (course == null) {
            log.info("Failed to check membership: Course does not exist.");
            return null;
        }

        String userId = session.getEmail();
        //check if the user is one of the course's intructors
        if (course.getInstructorIds().contains(userId)) {
            log.info("VALID MEMBER: INSTRUCTOR.");
            return instructorRepository.getInstructorElseNull(userId);
        }
        //else check if the user is one of the course's students
        if (course.getStudentIds().contains(userId)) {
            log.info("VALID MEMBER: STUDENT.");
            return studentRepository.getStudentElseNull(userId);
        }

        log.info("NOT A MEMBER");
        return null;
    }

    //============================================================================
    //MODIFY COURSE
    //============================================================================

    /**
     * Gets called when user enters inviteId and courseId in Join Course page (/course/join). If inviteId and courseId
     * are correct, the user is added to the course.
     *
     * @param courseId  32-bit courseId of the course
     * @param sessionId 64-bit user's sessionId
     * @param inviteId  64-bit inviteId sent by the inviting instructor to the user's email
     * @return true if user joins course successfully, false otherwise
     */
    @GetMapping("{courseId}/join")
    public boolean joinCourse(@PathVariable("courseId") String courseId,
                              @RequestParam("sessionId") String sessionId,
                              @RequestParam("inviteId") String inviteId) {
        //check if the session is valid
        Session session = sessionRepository.getSessionElseNull(sessionId);
        if (session == null) {
            log.info("Failed to join course: Session does not exist.");
            return false;
        }

        //check if the course exists
        Course course = courseRepository.getCourseElseNull(courseId);
        if (course == null) {
            log.info("Failed to join course: Course does not exist.");
            return false;
        }

        //check if the invite id exists
        CourseInviteToken courseInviteToken = courseInviteTokenRepository.getCourseInviteTokenElseNull(inviteId);
        if (courseInviteToken == null) {
            log.info("Failed to join course: InviteId does not exist.");
            return false;
        }

        //check the role
        if (courseInviteToken.getRole().equals("INSTRUCTOR")) {
            //add instructor to course
            course.addInstructor(courseInviteToken.getEmail());
            courseRepository.save(course);  //update
            log.info("ADDED INSTRUCTOR TO COURSE");

            //add courseInstructed to instructor
            Instructor instructor = instructorRepository.findById(courseInviteToken.getEmail()).get();
            instructor.addCourseInstructedIds(courseId);
            instructorRepository.save(instructor);
            log.info("ADDED COURSE INSTRUCTED TO INSTRUCTOR");

            courseInviteTokenRepository.delete(courseInviteToken); //clear token
            return true;
        } else if (courseInviteToken.getRole().equals("STUDENT")) {
            //add student to course
            course.addStudent(courseInviteToken.getEmail());
            courseRepository.save(course);  //update
            log.info("ADDED STUDENT TO COURSE");

            //add courseEnrolled to student
            Student student = studentRepository.findById(courseInviteToken.getEmail()).get();
            student.addCourseEnrolledIds(courseId);
            studentRepository.save(student);
            log.info("ADDED COURSE ENROLLED TO STUDENT");

            courseInviteTokenRepository.delete(courseInviteToken); //clear token
            return true;
        }
        log.info("Failed to join course: Invalid role.");
        return false;
    }

    /**
     * Add folders to the course. Folders are used to for tagging and filtering posts. Folders can be added only by
     * instructor of the course in the Course Settings page.
     *
     * @param courseId   32-bit courseId of the course
     * @param sessionId  64-bit user's sessionId
     * @param newFolders List of the name of the new folders.
     * @return all folders in the course after addition
     */
    @PostMapping("{courseId}/addfolders")
    public List<String> addFolders(
            @PathVariable("courseId") String courseId,
            @RequestParam("sessionId") String sessionId,
            @RequestBody List<String> newFolders) {

        if (newFolders == null) {
            log.info("Failed to add folders: Folder is null.");
            return null;
        }

        //check if the session is valid
        Session session = sessionRepository.getSessionElseNull(sessionId);
        if (session == null) {
            log.info("Failed to add folders: Session does not exist.");
            return null;
        }

        //check course exists
        Course course = courseRepository.getCourseElseNull(courseId);
        if (course == null) {
            log.info("Failed to get course: Course does not exist.");
            return null;
        }

        //check user is an instructor in the course
        if (course.getInstructorIds().contains(session.getEmail())) {
            log.info("Failed to add folders: Invalid session or user is not in the course");
            return null;
        }

        course.addFolders(newFolders);
        courseRepository.save(course);

        log.info("ADDED FOLDERS: " + course.getFolders());
        return course.getFolders();
    }

    /**
     * Called when an instructor creates a new Course. A new course will contain name, description, list of instructor
     * and student emails. For each instructor and student, send an invitation email to join the course.
     * Instructor/student will be added to Course only after invitation is confirmed.
     *
     * @param course    the new course to be added
     * @param sessionId 64-bit of the user's sessionId
     * @return the newly created course
     */
    @PostMapping("new/{sessionId}")
    public Course addCourse(@RequestBody Course course, @PathVariable("sessionId") String sessionId) {
        if (course == null
                || sessionId == null
                || course.getName() == null
                || course.getDescription() == null
                || course.getInstructorIds() == null
                || course.getStudentIds() == null
                || course.getQuestionRootList() == null
                || course.getName().length() == 0
                || course.getDescription().length() == 0
                || course.getInstructorIds().size() == 0
                || course.getQuestionRootList().size() != 0) {
            log.info("Failed to create course: Arguments supplied (other than courseId) contains null or invalid field .");
            return null;
        }

        //Check session exists
        Session session = sessionRepository.getSessionElseNull(sessionId);
        if (session == null) {
            log.info("Failed to create course: Session is invalid.");
            return null;
        }

        //Check the requester is an Instructor
        Instructor instructor = instructorRepository.getInstructorElseNull(session.getEmail());
        if (instructor == null) {
            log.info("Failed to create course: User does not exist or not an instructor.");
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

        //set course id
        course.setId(StringTools.generateID(32));

        //After course created, now send invitation emails to TAs, Students
        Map<String, CourseInviteToken> emailTokenMap = new HashMap<>();

        //prepare invitation to Instructors
        for (String email : course.getInstructorIds()) {
            //send to TAs/Instructor EXCEPT the creator
            if (!email.equals(course.getCreatorInstructorId())) {
                CourseInviteToken token = new CourseInviteToken(course.getId(), "INSTRUCTOR", email);
                courseInviteTokenRepository.save(token);
                emailTokenMap.put(email, token);
            }
        }

        //prepare invitation to Students
        for (String email : course.getStudentIds()) {
            CourseInviteToken token = new CourseInviteToken(course.getId(), "STUDENT", email);
            courseInviteTokenRepository.save(token);
            emailTokenMap.put(email, token);
        }

        //send token
        emailServices.sendCourseInviteToken(emailTokenMap, course.getName());

        //edit course's instructor, student list, then save course
        course.setStudentIds(new ArrayList<>());                            //since no student accepted invitation yet
        course.setInstructorIds(Collections.singletonList(course.getCreatorInstructorId()));   //save only the creator
        courseRepository.save(course);
        log.info("COURSE CREATED: " + course);

        //update creator's courseInstructedIds
        instructor.addCourseInstructedIds(course.getId());
        instructorRepository.save(instructor);  //update instructor, necessary?
        log.info("COURSE INSTRUCTED ADDED TO : " + instructor);

        return course;
    }


    /**
     * Delete course and all its contents. Only creator of the course can delete the course.
     *
     * @param courseId 32-bit courseId of the course
     * @param session  Session of the requester
     * @return true if the course is successfully deleted, otherwise false
     */
    @PostMapping("{courseId}/delete/course")
    @Transactional
    public boolean deleteCourse(@PathVariable("courseId") String courseId, @RequestBody Session session) {
        //Check session
        if (!sessionController.validate(session)) {
            log.info("Failed to delete course : Invalid session");
            return false;
        }

        //Check if course exists
        Course course = courseRepository.getCourseElseNull(courseId);
        if (course == null) {
            log.info("Failed to delete course : Course does not exist");
            return false;
        }

        //Check if user is the creator of the course
        if (!course.getCreatorInstructorId().equals(session.getEmail())) {
            log.info("Failed to delete course : You're not the creator of " + course.getName());
            return false;
        }

        //Remove course from all instructors' courseInstructed list
        for (User u : userRepository.findAllById(course.getInstructorIds())) {
            Instructor instructor = (Instructor) u;
            instructor.removeCourseInstructedIds(courseId);
            userRepository.save(instructor);
            log.info("COURSE DELETED FROM INSTRUCTOR: " + instructor);
        }

        //Remove course from all students' courseEnrolled list
        for (User u : userRepository.findAllById(course.getStudentIds())) {
            Student student = (Student) u;
            student.removeCourseEnrolledIds(courseId);
            userRepository.save(student);
            log.info("COURSE DELETED FROM STUDENT: " + student);
        }

        //Delete all posts associated with the course
        for (Post post : postRepository.findPostsByCourseId(courseId)) {
            postRepository.delete(post);
            log.info("POST DELETED: " + post);
        }

        courseRepository.delete(course);
        log.info("COURSE DELETED: " + course);
        return true;
    }


    /**
     * Send invitation emails to join a course. Recipient will receive email with invitationId and instructions on how
     * to login/register to Concourse and join the course using the inviteId. Only instructors can send invitation
     * emails through Course Settings.
     *
     * @param courseId  32-bit courseId of the course
     * @param emails    list of recipient emails
     * @param sessionId 32-bit char of the sender's session
     * @param role      the recipients' role in the course (STUDENT/INSTRUCTOR)
     * @return true if at invitation was sent, false otherwise
     */
    @PostMapping("{courseId}/invite")
    public boolean sendInvitation(
            @PathVariable("courseId") String courseId,
            @RequestBody List<String> emails,
            @RequestParam("sessionId") String sessionId,
            @RequestParam("role") String role
    ) {
        //Check if the session is valid
        Session session = sessionRepository.getSessionElseNull(sessionId);
        if (session == null) {
            log.info("Failed to send invitation: Invalid session");
            return false;
        }

        //Check if the course is present
        Course course = courseRepository.getCourseElseNull(courseId);
        if (course == null) {
            log.info("Failed to send invitation: Invalid course");
            return false;
        }

        //Check if user is an instructor of the courseId (only instructors can invite students/other instructors)
        if (!course.getInstructorIds().contains(session.getEmail())) {
            log.info("Failed to send invitation: User not an instructor of the course");
            return false;
        }

        //check the role of the invitee
        if (!role.equals("INSTRUCTOR") && !role.equals("STUDENT")) {
            log.info("Failed to send invitation: Invalid role");
            return false;
        }

        HashMap<String, CourseInviteToken> emailTokenMap = new HashMap<>();
        for (String email : emails) {
            //if email is never registered before
            if (!course.getInstructorIds().contains(email) && !course.getStudentIds().contains(email)) {
                CourseInviteToken token = new CourseInviteToken(course.getId(), role, email);
                courseInviteTokenRepository.save(token);
                emailTokenMap.put(email, token);
            }
        }

        //send email token
        emailServices.sendCourseInviteToken(emailTokenMap, course.getName());

        return true;
    }

    /**
     * Remove students from the course. Only instructors can remove students through Course Settings.
     *
     * @param courseId  32-bit courseId of the course
     * @param emails    list of emails of the students to be removed
     * @param sessionId 32-bit char of the sender's session
     * @return true if the students are removed from the course, false otherwise
     */
    @PostMapping("{courseId}/delete/students")
    public boolean removeStudents(
            @PathVariable("courseId") String courseId,
            @RequestBody List<String> emails,
            @RequestParam("sessionId") String sessionId) {
        //check if session is valid
        Session session = sessionRepository.getSessionElseNull(sessionId);
        if (session == null) {
            log.info("Failed to remove students: Invalid session");
            return false;
        }

        //check if the email object is valid
        if (emails == null || emails.size() == 0) {
            log.info("Failed to remove students: No emails found");
            return false;
        }

        //check if the course exists
        Course course = courseRepository.getCourseElseNull(courseId);
        if (course == null) {
            log.info("Failed to remove students: Invalid course");
            return false;
        }

        //check if the requester's is an instructor of the course
        if (!course.getInstructorIds().contains(session.getEmail())) {
            log.info("Failed to remove students: Not an instructor");
            return false;
        }

        //remove students from course list
        List<String> studentIds = course.getStudentIds();
        studentIds.removeAll(emails);
        course.setStudentIds(studentIds);
        log.info("USER REMOVED FROM COURSE: " + course);
        courseRepository.save(course);

        //remove course from students' enrolled course list and delete students' posts in the course
        for (String email : emails) {
            Student student = studentRepository.getStudentElseNull(email);
            if (student != null) {
                student.removeCourseEnrolledIds(courseId);
                studentRepository.save(student);

                //delete content of the posts belonging to the students
                List<Post> studentPostsInCourse =
                        postRepository.findPostsByAuthorUserIdAndCourseId(student.getEmail(), courseId);
                for (Post p : studentPostsInCourse) {
                    //remove contents and attributes of posts posted by the deleted student
                    p.setContent("[DELETED]");
                    p.setAuthorName("[DELETED]");
                    p.setAuthorUserId("[DELETED]");
                    postRepository.save(p);
                }
            } else {
                log.info("Failed to delete student: Student with " + email + " does not exists");
            }
        }
        return true;
    }
}
