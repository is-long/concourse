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


/*
TODO: REFACTOR

*/

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
    private QuestionRootRepository questionRootRepository;
    private EmailServices emailServices;
    private QuestionRootAnswerRepository questionRootAnswerRepository;
    private QuestionRootAnswerReplyRepository questionRootAnswerReplyRepository;
    private FollowupQuestionRepository followupQuestionRepository;
    private FollowupAnswerRepository followupAnswerRepository;

    public CourseController(UserRepository userRepository, CourseRepository courseRepository, SessionRepository sessionRepository, PostRepository postRepository,
                            SessionController sessionController, StudentRepository studentRepository, InstructorRepository instructorRepository, CourseInviteTokenRepository courseInviteTokenRepository, QuestionRootRepository questionRootRepository, EmailServices emailServices, QuestionRootAnswerRepository questionRootAnswerRepository, QuestionRootAnswerReplyRepository questionRootAnswerReplyRepository, FollowupQuestionRepository followupQuestionRepository, FollowupAnswerRepository followupAnswerRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
        this.postRepository = postRepository;
        this.sessionController = sessionController;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.courseInviteTokenRepository = courseInviteTokenRepository;
        this.questionRootRepository = questionRootRepository;
        this.emailServices = emailServices;
        this.questionRootAnswerRepository = questionRootAnswerRepository;
        this.questionRootAnswerReplyRepository = questionRootAnswerReplyRepository;
        this.followupQuestionRepository = followupQuestionRepository;
        this.followupAnswerRepository = followupAnswerRepository;
    }


    //============================================================================
    //ACCESS COURSE
    //============================================================================

    @GetMapping("all")
    public List<Course> getAll(){
        return (List<Course>)courseRepository.findAll();
    }

    @PostMapping("{courseId}/get")
    public Course getCourse(@PathVariable("courseId") String courseId, @RequestBody Session session) {
        if (session == null || session.getSessionId() == null) {
            log.info("Failed to get course: Invalid session.");
            return null;
        }

        //if the user of the session is involved in the course
        if (checkMember(courseId, session.getSessionId()) != null) {
            Optional<Course> optionalCourse =  this.courseRepository.findById(courseId);  //null already checked by check member
            if (optionalCourse.isPresent()){
                Course c = optionalCourse.get();
                log.info("FETCH COURSE: " + c);
                return c;
            }
        }

        log.info("User is not involved in the course.");
        return null;
    }

    @PostMapping("{courseId}/getfolders")
    public List<String> getFolders(
            @PathVariable("courseId") String courseId,
            @RequestBody Session session
    ) {
        String sessionId = session.getSessionId();
        //check session, is involved
        log.info("Trying to getFolders for course: " + courseId);
        if (courseId.length() != 32 || sessionId.length() != 64) {
            log.info("Failed to getFolders: Invalid arguments");
            return null;
        }
        //check user is involved in course, course exists
        if (checkMember(courseId, sessionId) == null) {
            log.info("Failed to add view: Invalid session or user is not in the course");
            return null;
        }
        List<String> folders = courseRepository.findById(courseId).get().getFolders();
        log.info("RETURNING FOLDERS: " + folders);
        return folders;
    }

    @GetMapping("{courseId}/checkmember/{sessionId}")
    public User checkMember(@PathVariable("courseId") String courseId, @PathVariable String sessionId) {
        Optional<Session> optionalSession = sessionRepository.findById(sessionId);
        if (!optionalSession.isPresent()) {
            log.info("Failed to check member: Invalid session");
            return null;
        }
        Session session = optionalSession.get();
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent()) {
            log.info("Failed to check member: Course does not exist.");
            return null;
        }
        Course course = optionalCourse.get();
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
        if (checkMember(courseId, sessionId) == null) {
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


    //============================================================================
    //NEW POSTS; TODO MOVE TO POSTCONTROLLER
    //============================================================================

    @PostMapping("{courseId}/post/new/questionroot")
    public QuestionRoot addQuestionRoot(@PathVariable("courseId") String courseId, @RequestParam("sessionId") String sessionId,
                                        @RequestBody QuestionRoot questionRoot) {
        log.info("Trying to add question root: " + questionRoot);
        if (courseId.length() != 32 || sessionId.length() != 64 || questionRoot == null) {
            log.info("Failed to add question root: Invalid arguments");
            return null;
        }

        //check if session is valid
        //check if user is involved
        if (checkMember(courseId, sessionId) == null) {
            log.info("Failed to add question root: Invalid session or user is not in the course");
            return null;
        }

        //check if question root is not empty
        if (!questionRoot.getCourseId().equals(courseId)) {
            log.info("Failed to add question root: Course ids don't match");
            return null;
        }
        //check if author exists
        Optional<User> optionalUser = userRepository.findById(questionRoot.getAuthorUserId());
        if (questionRoot.getAuthorUserId() == null || !optionalUser.isPresent()) {
            log.info("Failed to add question root: Invalid author");
            return null;
        }
        if (questionRoot.getContent() == null || questionRoot.getContent().length() == 0
                || questionRoot.getTitle() == null || questionRoot.getTitle().length() == 0) {
            log.info("Failed to add question root: Empty content/title");
            return null;
        }
        User user = optionalUser.get();
        questionRoot.setId(StringTools.generateID(32));
        questionRoot.setAuthorName(user.getName());
        questionRoot.setAuthorType(user.getRole());
        questionRoot.setPostDate(new Date().getTime());
        questionRoot.setViewCount(0);
        questionRoot.setViewerIds(new ArrayList<>());
        questionRoot.setFollowupQuestionList(new ArrayList<>());
        questionRoot.setQuestionRootAnswerList(new ArrayList<>());
//        questionRoot.setHasInstructorAnswer();

        questionRootRepository.save(questionRoot);
        courseRepository.saveQuestionRootToCourse(courseId, questionRoot);
        log.info("QUESTION ROOT ADDED: " + questionRoot);

        return questionRoot;
    }

    @PostMapping("{courseId}/post/new/questionrootanswer")
    public QuestionRootAnswer addQuestionRootAnswer(@PathVariable("courseId") String courseId,
                                                    @RequestParam("sessionId") String sessionId,
                                                    @RequestBody QuestionRootAnswer questionRootAnswer) {
        //check session, is involved
        log.info("Trying to add question root answer: " + questionRootAnswer);
        if (courseId.length() != 32 || sessionId.length() != 64 || questionRootAnswer == null) {
            log.info("Failed to add question root: Invalid arguments");
            return null;
        }
        //check user is involved in course
        if (checkMember(courseId, sessionId) == null) {
            log.info("Failed to add question root answer: Invalid session or user is not in the course");
            return null;
        }
        //check questionrootanswer
        if (questionRootAnswer.getAuthorUserId() == null
                || questionRootAnswer.getAuthorName() == null
                || questionRootAnswer.getAuthorType() == null
                || questionRootAnswer.getQuestionRootId() == null
                || questionRootAnswer.getContent() == null
                || questionRootAnswer.getContent().length() == 0
                || questionRootAnswer.getCourseId() == null) {
            log.info("Failed to add question root answer: Invalid question root answer properties");
            return null;
        }
        questionRootAnswer.setId(new QuestionRootAnswer().getId());

        //check parent exists
        Optional<QuestionRoot> optionalQuestionRoot = questionRootRepository.findById(questionRootAnswer.getQuestionRootId());
        if (!optionalQuestionRoot.isPresent() || !optionalQuestionRoot.get().getCourseId().equals(questionRootAnswer.getCourseId())) {
            log.info("Failed to add question root answer: Invalid question root parent");
            return null;
        }

        //check author valid
        Optional<User> optionalUser = userRepository.findById(questionRootAnswer.getAuthorUserId());
        if (!optionalUser.isPresent()) {
            log.info("Failed to add question root answer: Invalid author");
            return null;
        }
        //set author details from server
        questionRootAnswer.setAuthorName(optionalUser.get().getName());
        questionRootAnswer.setAuthorType(optionalUser.get().getRole());
        questionRootAnswer.setQuestionRootAnswerReplyList(new ArrayList<>());

        //save answer
        postRepository.save(questionRootAnswer);

        //add to parent, then save parent
        QuestionRoot questionRoot = optionalQuestionRoot.get();
        questionRoot.addQuestionRootAnswer(questionRootAnswer);
        questionRootRepository.save(questionRoot);

        //save to course
        Course course = courseRepository.findById(courseId).get();  //already null check in check member
        course.replaceQuestionRoot(questionRoot.getId(), questionRoot);  //replace old with new
        courseRepository.save(course);  //save

        log.info("SAVED QUESTION ROOT ANSWER: " + questionRootAnswer);
        return questionRootAnswer;
    }

    @PostMapping("{courseId}/post/new/questionrootanswerreply")
    public QuestionRootAnswerReply addQuestionRootAnswerReply(@PathVariable("courseId") String courseId,
                                                              @RequestParam("sessionId") String sessionId,
                                                              @RequestBody QuestionRootAnswerReply questionRootAnswerReply) {
        //check session, is involved
        log.info("Trying to add question root answer reply: " + questionRootAnswerReply);
        if (courseId.length() != 32 || sessionId.length() != 64 || questionRootAnswerReply == null) {
            log.info("Failed to add question root answer reply: Invalid arguments");
            return null;
        }
        //check user is involved in course
        if (checkMember(courseId, sessionId) == null) {
            log.info("Failed to add question root answer reply: Invalid session or user is not in the course");
            return null;
        }
        //check questionrootanswer
        if (questionRootAnswerReply.getAuthorUserId() == null
                || questionRootAnswerReply.getAuthorName() == null
                || questionRootAnswerReply.getAuthorType() == null
                || questionRootAnswerReply.getQuestionRootAnswerId() == null
                || questionRootAnswerReply.getContent() == null
                || questionRootAnswerReply.getContent().length() == 0
                || questionRootAnswerReply.getCourseId() == null
        ) {
            log.info("Failed to add question answer reply: Invalid properties");
            return null;
        }

        //check question root parent answer exists

        Optional<QuestionRootAnswer> optionalQuestionRootAnswer = questionRootAnswerRepository.findById(
                questionRootAnswerReply.getQuestionRootAnswerId());
        if (!optionalQuestionRootAnswer.isPresent()) {
            log.info("Failed to add question answer reply: Parent not found");
            return null;
        }


        //check author valid
        Optional<User> optionalUser = userRepository.findById(questionRootAnswerReply.getAuthorUserId());
        if (!optionalUser.isPresent()) {
            log.info("Failed to add followup question: Invalid author");
            return null;
        }

        //set author details from server
        questionRootAnswerReply.setAuthorName(optionalUser.get().getName());
        questionRootAnswerReply.setAuthorType(optionalUser.get().getRole());

        //save answer
        postRepository.save(questionRootAnswerReply);

        //add to parent, then save parent
        QuestionRootAnswer qra = optionalQuestionRootAnswer.get();
        qra.addQuestionRootAnswerReply(questionRootAnswerReply);
        questionRootAnswerRepository.save(qra);

        //save to question root
        QuestionRoot qr = questionRootRepository.findById(qra.getQuestionRootId()).get();
        qr.replaceQuestionRootAnswer(qra.getId(), qra);

        //save to course
        Course course = courseRepository.findById(courseId).get();  //already null check in check member
        course.replaceQuestionRoot(qr.getId(), qr);  //replace old with new
        courseRepository.save(course);  //save

        return questionRootAnswerReply;
    }

    @PostMapping("{courseId}/post/new/followupquestion")
    public FollowupQuestion addFollowupQuestion(@PathVariable("courseId") String courseId,
                                                @RequestParam("sessionId") String sessionId,
                                                @RequestBody FollowupQuestion followupQuestion) {
        //check session, is involved
        log.info("Trying to add followup question: " + followupQuestion);
        if (courseId.length() != 32 || sessionId.length() != 64 || followupQuestion == null) {
            log.info("Failed to add question root: Invalid arguments");
            return null;
        }
        //check user is involved in course
        if (checkMember(courseId, sessionId) == null) {
            log.info("Failed to add followup question: Invalid session or user is not in the course");
            return null;
        }
        //check questionrootanswer
        if (followupQuestion.getAuthorUserId() == null
                || followupQuestion.getAuthorName() == null
                || followupQuestion.getAuthorType() == null
                || followupQuestion.getQuestionRootId() == null
                || followupQuestion.getContent() == null
                || followupQuestion.getContent().length() == 0
                || followupQuestion.getCourseId() == null
        ) {
            log.info("Failed to add followup question: Invalid followup question properties");
            return null;
        }
        followupQuestion.setId(new QuestionRootAnswer().getId());

        //check parent exists
        Optional<QuestionRoot> optionalQuestionRoot = questionRootRepository.findById(followupQuestion.getQuestionRootId());
        if (!optionalQuestionRoot.isPresent() || !optionalQuestionRoot.get().getCourseId().equals(followupQuestion.getCourseId())) {
            log.info("Failed to add followup question: Invalid question root parent");
            return null;
        }

        //check author valid
        Optional<User> optionalUser = userRepository.findById(followupQuestion.getAuthorUserId());
        if (!optionalUser.isPresent()) {
            log.info("Failed to add followup question: Invalid author");
            return null;
        }

        //set author details from server
        followupQuestion.setAuthorName(optionalUser.get().getName());
        followupQuestion.setAuthorType(optionalUser.get().getRole());
        followupQuestion.setFollowupAnswerList(new ArrayList<>());

        //save answer
        postRepository.save(followupQuestion);

        //add to parent, then save parent
        QuestionRoot questionRoot = optionalQuestionRoot.get();
        questionRoot.addFollowupQuestion(followupQuestion);
        questionRootRepository.save(questionRoot);

        //save to course
        Course course = courseRepository.findById(courseId).get();  //already null check in check member
        course.replaceQuestionRoot(questionRoot.getId(), questionRoot);  //replace old with new
        courseRepository.save(course);  //save

        log.info("SAVED FOLLOW UP QUESTION: " + followupQuestion);
        return followupQuestion;
    }

    @PostMapping("{courseId}/post/new/followupanswer")
    public FollowupAnswer addFollowupAnswer(@PathVariable("courseId") String courseId,
                                            @RequestParam("sessionId") String sessionId,
                                            @RequestBody FollowupAnswer followupAnswer) {
        //check session, is involved
        log.info("Trying to add followup answer: " + followupAnswer);
        if (courseId.length() != 32 || sessionId.length() != 64 || followupAnswer == null) {
            log.info("Failed to add question root: Invalid arguments");
            return null;
        }
        //check user is involved in course
        if (checkMember(courseId, sessionId) == null) {
            log.info("Failed to add followup question: Invalid session or user is not in the course");
            return null;
        }
        //check questionrootanswer
        if (followupAnswer.getAuthorUserId() == null
                || followupAnswer.getAuthorName() == null
                || followupAnswer.getAuthorType() == null
                || followupAnswer.getFollowupQuestionId() == null
                || followupAnswer.getContent() == null
                || followupAnswer.getContent().length() == 0
                || followupAnswer.getCourseId() == null
        ) {
            log.info("Failed to add followup question: Invalid followup question properties");
            return null;
        }

        followupAnswer.setId(new FollowupAnswer().getId());

        //check parent exists
        Optional<FollowupQuestion> optionalFollowupQuestion = followupQuestionRepository.findById(followupAnswer.getFollowupQuestionId());
        if (!optionalFollowupQuestion.isPresent() || !optionalFollowupQuestion.get().getCourseId().equals(followupAnswer.getCourseId())) {
            log.info("Failed to add followup question: Invalid followup question parent");
            return null;
        }

        //check author valid
        Optional<User> optionalUser = userRepository.findById(followupAnswer.getAuthorUserId());
        if (!optionalUser.isPresent()) {
            log.info("Failed to add followup question: Invalid author");
            return null;
        }

        //set author details from server
        followupAnswer.setAuthorName(optionalUser.get().getName());
        followupAnswer.setAuthorType(optionalUser.get().getRole());

        //save answer
        postRepository.save(followupAnswer);

        FollowupQuestion fq = optionalFollowupQuestion.get();
        fq.addFollowupAnswer(followupAnswer);
        followupQuestionRepository.save(fq);

        QuestionRoot qr = questionRootRepository.findById(fq.getQuestionRootId()).get();
        qr.replaceFollowupQuestion(fq.getId(), fq);
        questionRootRepository.save(qr);

        Course c = courseRepository.findById(qr.getCourseId()).get();
        c.replaceQuestionRoot(qr.getId(), qr);
        courseRepository.save(c);

        log.info("SAVED FOLLOW UP ANSWER : " + followupAnswer);
        return followupAnswer;
    }


    //============================================================================
    //MODIFY POSTS
    //============================================================================


    //Add view count to question root
    @PostMapping("{courseId}/post/{postId}/view")
    public boolean view(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestBody Session session
    ) {
        String sessionId = session.getSessionId();
        //check session, is involved
        log.info("Trying to add view: " + postId);
        if (courseId.length() != 32 || sessionId.length() != 64 || postId == null) {
            log.info("Failed to add view: Invalid arguments");
            return false;
        }

        //check user is involved in course
        if (checkMember(courseId, sessionId) == null) {
            log.info("Failed to add view: Invalid session or user is not in the course");
            return false;
        }

        try {
            Optional<QuestionRoot> optionalQuestionRoot = questionRootRepository.findById(postId);
            log.info("IN VIEW 4, OQR" + optionalQuestionRoot);
            if (!optionalQuestionRoot.isPresent()) {
                log.info("Failed to add view: Question root not found");
                return false;
            }
            log.info("IN VIEW 5, OQR is present");
            QuestionRoot qr = optionalQuestionRoot.get();
            qr.addViewerId(session.getEmail());
            questionRootRepository.save(qr);

            return true;
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @PostMapping("{courseId}/post/{postId}/like")
    public boolean like(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestBody Session session,
            @RequestParam("value") String value
    ) {
        String sessionId = session.getSessionId();

        log.info("Trying to create like to post: " + postId);
        if (courseId.length() != 32 || postId.length() != 32
                || sessionId == null || sessionId.length() != 64
                || value == null) {
            log.info("Failed to like post: Invalid arguments");
            return false;
        }
        Integer i;
        try {
            i = Integer.valueOf(value);
            if (i < -1 || i > 1) {
                log.info("Failed to like post: Invalid arguments");
                return false;
            }
        } catch (Exception e) {
            log.info("Failed to like post: Invalid arguments");
            return false;
        }

        //check user is involved in course
        if (checkMember(courseId, sessionId) == null) {
            log.info("Failed to like post: Invalid session or user is not in the course");
            return false;
        }

        //check post exists
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (!optionalPost.isPresent()) {
            log.info("Failed to like post: Post does not exists");
            return false;
        }

        String userId = sessionRepository.findById(sessionId).get().getEmail();
        Post post = optionalPost.get();
        post.like(userId, i);
        postRepository.save(post);
        return true;
    }


    @PostMapping("{courseId}/post/{postId}/delete")
    public boolean delete(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestParam("postType") String postType,
            @RequestBody Session session
    ) {
        //check if is the instructor OR the user's own post
        if (session == null || courseId.length() != 32 || postId.length() != 32 || postType == null) {
            log.info("Failed to delete post: Invalid arguments");
            return false;
        }

        Optional<Session> optionalSession = sessionRepository.findById(session.getSessionId());
        if (!optionalSession.isPresent() || !optionalSession.get().getEmail().equals(session.getEmail())) {
            log.info("Failed to delete post: Invalid session");
            return false;
        }

        User user = userRepository.findById(session.getEmail()).get();  //if session exists, user exists

        Optional<Post> optionalPost = postRepository.findById(postId);
        if (!optionalPost.isPresent() || !optionalPost.get().getCourseId().equals(courseId)) {
            log.info("Failed to delete post: Post not found or invalid");
            return false;
        }

        if (!user.getRole().equals("INSTRUCTOR") && !optionalPost.get().getAuthorUserId().equals(session.getEmail())) {
            log.info("Failed to delete post: Unauthorized");
            return false;
        }

        switch (postType) {
            case "QUESTIONROOT":
                Optional<QuestionRoot> optionalQuestionRoot = questionRootRepository.findById(postId);
                if (!optionalQuestionRoot.isPresent()) {
                    log.info("Failed to delete post: Invalid type");
                    return false;
                }
                Course c = courseRepository.findById(courseId).get();
                c.removeQuestionRoot(optionalQuestionRoot.get());
                courseRepository.save(c);

                questionRootRepository.deleteById(postId);
                log.info("REMOVED QUESTION ROOT: " + postId);
                return true;
            case "QUESTIONROOTANSWER": {
                Optional<QuestionRootAnswer> optionalQuestionRootAnswer = questionRootAnswerRepository.findById(postId);
                if (!optionalQuestionRootAnswer.isPresent()) {
                    log.info("Failed to delete post: Invalid type");
                    return false;
                }
                QuestionRoot qr = questionRootRepository.findById(optionalQuestionRootAnswer.get().getQuestionRootId()).get();
                qr.removeQuestionRootAnswer(optionalQuestionRootAnswer.get());
                questionRootRepository.save(qr);

                questionRootAnswerRepository.deleteById(postId);
                log.info("REMOVED QUESTION ROOT ANSWER: " + postId);
                return true;
            }
            case "QUESTIONROOTANSWERREPLY":
                Optional<QuestionRootAnswerReply> optionalQuestionRootAnswerReply = questionRootAnswerReplyRepository.findById(postId);
                if (!optionalQuestionRootAnswerReply.isPresent()) {
                    log.info("Failed to delete post: Invalid type");
                    return false;
                }
                QuestionRootAnswer qra = questionRootAnswerRepository.findById(
                        optionalQuestionRootAnswerReply.get().getQuestionRootAnswerId()).get();
                qra.removeQuestionRootAnswerReply(optionalQuestionRootAnswerReply.get());

                questionRootAnswerRepository.save(qra);

                questionRootAnswerReplyRepository.deleteById(postId);
                log.info("REMOVED QUESTION ROOT ANSWER REPLY: " + postId);
                return true;
            case "FOLLOWUPQUESTION": {
                Optional<FollowupQuestion> optionalFollowupQuestion = followupQuestionRepository.findById(postId);
                if (!optionalFollowupQuestion.isPresent()) {
                    log.info("Failed to delete post: Invalid type");
                    return false;
                }
                QuestionRoot qr = questionRootRepository.findById(optionalFollowupQuestion.get().getQuestionRootId()).get();
                qr.removeFollowupQuestion(optionalFollowupQuestion.get());
                questionRootRepository.save(qr);


                followupQuestionRepository.deleteById(postId);
                log.info("REMOVED FOLLOWUP QUESTION: " + postId);
                return true;

            }
            case "FOLLOWUPANSWER":

                Optional<FollowupAnswer> optionalFollowupAnswer = followupAnswerRepository.findById(postId);
                if (!optionalFollowupAnswer.isPresent()) {
                    log.info("Failed to delete post: Invalid type");
                    return false;
                }
                FollowupQuestion fq = followupQuestionRepository.findById(optionalFollowupAnswer.get().getFollowupQuestionId()).get();
                fq.removeFollowupAnswer(optionalFollowupAnswer.get());
                followupQuestionRepository.save(fq);

                followupAnswerRepository.deleteById(postId);
                log.info("REMOVED FOLLOWUP ANSWER: " + postId);
                return true;

        }
        log.info("Failed to delete post: Invalid post type");
        return false;
    }

    @PostMapping("{courseId}/post/{postId}/checkOwnership")
    public boolean checkPostOwnership(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestBody Session session
    ) {
        if (session == null || courseId.length() != 32 || postId.length() != 32
        ) {
            log.info("Failed to check ownership: Invalid arguments");
            return false;
        }

        Optional<Session> optionalSession = sessionRepository.findById(session.getSessionId());
        if (!optionalSession.isPresent() || !optionalSession.get().getEmail().equals(session.getEmail())) {
            log.info("Failed to check ownership: Invalid session");
            return false;
        }

        User user = userRepository.findById(session.getEmail()).get();  //if session exists, user exists

        Optional<Post> optionalPost = postRepository.findById(postId);
        if (!optionalPost.isPresent()) {
            log.info("Failed to check ownership: Post not found or invalid");
            return false;
        }
        Post post = optionalPost.get();
        if (!post.getCourseId().equals(courseId)) {
            log.info("Failed to check ownership: Post course does not exists");
            return false;
        }

        if (!optionalPost.get().getAuthorUserId().equals(session.getEmail())) {
            log.info("Failed to check ownership: Unauthorized");
            return false;
        }
        return true;
    }


    @PostMapping("{courseId}/post/{postId}/get")
    public Post getPost(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestBody Session session) {
        if (checkMember(courseId, session.getSessionId()) == null) {
            log.info("Failed to get post: User is not a member of the course or has invalid session");
            return null;
        }
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (!optionalPost.isPresent()) {
            log.info("Failed to get post: Post not found");
            return null;
        }

        Post post = optionalPost.get();
        log.info("FETCH POST: " + post);
        return post;
    }

    @PostMapping("{courseId}/post/{postId}/edit")
    public boolean editPost(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestParam("sessionId") String sessionId,
            @RequestBody Post modifiedPost) {
        Session session = sessionRepository.findById(sessionId).get();
        if (!checkPostOwnership(courseId, postId, session)) {
            log.info("Failed to edit post: Unauthorized user");
            return false;
        }

        Optional<Post> optionalPost = postRepository.findById(postId);
        if (!optionalPost.isPresent()) {
            log.info("Failed to edit post: Post does not exist");
            return false;
        }

        Post post = optionalPost.get();
        post.setContent(modifiedPost.getContent());
        postRepository.save(post);
        log.info("EDIT POST: " + post);
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
        if (!optionalSession.isPresent()){
            log.info("Failed to remove students: Invalid session");
            return false;
        }
        Session session = optionalSession.get();

        if (emails == null || emails.size() == 0){
            log.info("Failed to remove students: No emails found");
            return false;
        }

        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent()){
            log.info("Failed to remove students: Invalid course");
            return false;
        }
        Course course = optionalCourse.get();
        if (!course.getInstructorIds().contains(session.getEmail())){
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
        for (String email: emails) {
            Optional<Student> optionalStudent = studentRepository.findById(email);
            if (optionalStudent.isPresent()){
                Student student = optionalStudent.get();
                student.removeCourseEnrolledIds(courseId);
                studentRepository.save(student);

                //delete content of the posts belonging to the students
                List<Post> studentPostsInCourse = postRepository.findPostsByAuthorUserIdAndCourseId(student.getEmail(), courseId);
                for (Post p: studentPostsInCourse) {
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
