package com.concourse.controllers;

import com.concourse.models.Course;
import com.concourse.models.Session;
import com.concourse.models.posts.*;
import com.concourse.models.users.User;
import com.concourse.repository.*;
import com.concourse.tools.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("course")
@CrossOrigin(origins = "*", allowedHeaders = "*", allowCredentials = "true")
public class PostController {
    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private SessionRepository sessionRepository;
    private PostRepository postRepository;
    private QuestionRootRepository questionRootRepository;
    private QuestionRootAnswerRepository questionRootAnswerRepository;
    private QuestionRootAnswerReplyRepository questionRootAnswerReplyRepository;
    private FollowupQuestionRepository followupQuestionRepository;
    private FollowupAnswerRepository followupAnswerRepository;
    private CourseController courseController;

    public PostController(UserRepository userRepository, CourseRepository courseRepository,
                          SessionRepository sessionRepository, PostRepository postRepository,
                          QuestionRootRepository questionRootRepository,
                          QuestionRootAnswerRepository questionRootAnswerRepository,
                          QuestionRootAnswerReplyRepository questionRootAnswerReplyRepository,
                          FollowupQuestionRepository followupQuestionRepository,
                          FollowupAnswerRepository followupAnswerRepository, CourseController courseController) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
        this.postRepository = postRepository;
        this.questionRootRepository = questionRootRepository;
        this.questionRootAnswerRepository = questionRootAnswerRepository;
        this.questionRootAnswerReplyRepository = questionRootAnswerReplyRepository;
        this.followupQuestionRepository = followupQuestionRepository;
        this.followupAnswerRepository = followupAnswerRepository;
        this.courseController = courseController;
    }

    //============================================================================
    // ACCESS POSTS;
    //============================================================================

    /**
     * Access a post. User must be involved in the course to be able to fetch the post.
     * @param courseId id of the course
     * @param postId id of the post
     * @param session user's session
     * @return post object if user is authorized and post exists, otherwise null
     */
    @PostMapping("{courseId}/post/{postId}/get")
    public Post getPost(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestBody Session session) {
        if (courseController.checkCourseMembership(courseId, session.getSessionId()) == null) {
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

    //============================================================================
    //NEW POSTS;
    //============================================================================

    /**
     * Adds a new question root. Only students/instructors involved in the course can add a question.
     *
     * @param courseId id of the course
     * @param sessionId sessionId of the poster
     * @param questionRoot the question root to be added
     * @return the added question root if addition was successful, otherwise null
     */
    @PostMapping("{courseId}/post/new/questionroot")
    public QuestionRoot addQuestionRoot(@PathVariable("courseId") String courseId, @RequestParam("sessionId") String sessionId,
                                        @RequestBody QuestionRoot questionRoot) {
        if (questionRoot.getContent() == null
                || questionRoot.getContent().length() == 0
                || questionRoot.getTitle() == null
                || questionRoot.getTitle().length() == 0
                || questionRoot.getAuthorUserId() == null) {
            log.info("Failed to add question root: Empty content/title");
            return null;
        }

        //check if course exists, session is valid, and user is involved
        User user = courseController.checkCourseMembership(courseId, sessionId);
        if (user == null) {
            log.info("Failed to add question root: Invalid session or user");
            return null;
        }

        //check if the properties match
        Course course = courseRepository.getCourseElseNull(courseId);
        if (!questionRoot.getCourseId().equals(courseId)
                || !questionRoot.getAuthorUserId().equals(user.getEmail())
                || !course.getFolders().contains(questionRoot.getFolder())) {
            log.info("Failed to add question root: Field property mismatch");
            return null;
        }

        questionRoot.setId(StringTools.generateID(32));
        questionRoot.setAuthorName(user.getName());
        questionRoot.setAuthorType(user.getRole());
        questionRoot.setPostDate(new Date().getTime());
        questionRoot.setViewCount(0);
        questionRoot.setViewerIds(new ArrayList<>());
        questionRoot.setFollowupQuestionList(new ArrayList<>());
        questionRoot.setQuestionRootAnswerList(new ArrayList<>());

        //save/update objects
        questionRootRepository.save(questionRoot);
        courseRepository.saveQuestionRootToCourse(courseId, questionRoot);
        log.info("QUESTION ROOT ADDED: " + questionRoot);

        return questionRoot;
    }

    /**
     * Adds a new question root answer. Only students/instructors involved in the course can add a question root answer.
     *
     * @param courseId id of the course
     * @param sessionId sessionId of the poster
     * @param questionRootAnswer the question root answer to be added
     * @return the added question root answer if addition was successful, otherwise null
     */
    @PostMapping("{courseId}/post/new/questionrootanswer")
    public QuestionRootAnswer addQuestionRootAnswer(@PathVariable("courseId") String courseId,
                                                    @RequestParam("sessionId") String sessionId,
                                                    @RequestBody QuestionRootAnswer questionRootAnswer) {
        //check questionrootanswer
        if (questionRootAnswer == null
                || questionRootAnswer.getAuthorUserId() == null
                || questionRootAnswer.getQuestionRootId() == null
                || questionRootAnswer.getContent() == null
                || questionRootAnswer.getContent().length() == 0
                || questionRootAnswer.getCourseId() == null
                || !questionRootAnswer.getCourseId().equals(courseId)) {
            log.info("Failed to add question root answer: Invalid question root answer properties");
            return null;
        }

        //check user is involved in course
        User user = courseController.checkCourseMembership(courseId, sessionId);
        if (user == null) {
            log.info("Failed to add question root answer: Invalid session or user is not in the course");
            return null;
        }
        //if involved, set author details from server
        questionRootAnswer.setAuthorName(user.getName());
        questionRootAnswer.setAuthorType(user.getRole());
        questionRootAnswer.setQuestionRootAnswerReplyList(new ArrayList<>());
        questionRootAnswer.setId(new QuestionRootAnswer().getId());

        //check parent exists
        Optional<QuestionRoot> optionalQuestionRoot = questionRootRepository.findById(questionRootAnswer.getQuestionRootId());
        if (!optionalQuestionRoot.isPresent() || !optionalQuestionRoot.get().getCourseId()
                .equals(questionRootAnswer.getCourseId())) {
            log.info("Failed to add question root answer: Invalid question root parent");
            return null;
        }

        //BEGIN UPDATING/SAVING OBJECTS
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


    /**
     * Adds a new question root answer reply. Only students/instructors involved in the course can
     * add a question root answer reply.
     *
     * @param courseId id of the course
     * @param sessionId sessionId of the poster
     * @param questionRootAnswerReply the question root answer to be added
     * @return the added question root answer reply if addition was successful, otherwise null
     */
    @PostMapping("{courseId}/post/new/questionrootanswerreply")
    public QuestionRootAnswerReply addQuestionRootAnswerReply(@PathVariable("courseId") String courseId,
                                                              @RequestParam("sessionId") String sessionId,
                                                              @RequestBody QuestionRootAnswerReply questionRootAnswerReply) {
        //check questionrootanswer
        if (questionRootAnswerReply == null
                || questionRootAnswerReply.getAuthorUserId() == null
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

        //check user is involved in course
        User user =  courseController.checkCourseMembership(courseId, sessionId);
        if (user == null || !user.getEmail().equals(questionRootAnswerReply.getAuthorUserId())) {
            log.info("Failed to add question root answer reply: Invalid session or user");
            return null;
        }

        //check question root parent answer exists
        Optional<QuestionRootAnswer> optionalQuestionRootAnswer = questionRootAnswerRepository.findById(
                questionRootAnswerReply.getQuestionRootAnswerId());
        if (!optionalQuestionRootAnswer.isPresent()) {
            log.info("Failed to add question answer reply: Parent not found");
            return null;
        }

        //set author details from server
        questionRootAnswerReply.setAuthorName(user.getName());
        questionRootAnswerReply.setAuthorType(user.getRole());

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

    /**
     * Adds a new followup question to question root. Only students/instructors involved in the course can
     * add a followup question.
     *
     * @param courseId id of the course
     * @param sessionId sessionId of the poster
     * @param followupQuestion the followup question to be added
     * @return the added followup question if addition was successful, otherwise null
     */
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
        if (courseController.checkCourseMembership(courseId, sessionId) == null) {
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

    /**
     * Adds a new followup answer to followup question. Only students/instructors involved in the course can
     * add a followup answer.
     *
     * @param courseId id of the course
     * @param sessionId sessionId of the poster
     * @param followupAnswer the followup question to be added
     * @return the added followup answer if addition was successful, otherwise null
     */
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
        if (courseController.checkCourseMembership(courseId, sessionId) == null) {
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
                || followupAnswer.getCourseId() == null) {
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

        //save answer and modify parents
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

    /**
     * Adds view count to the post. View count is added when a user visits a newly seen question root page.
     *
     * @param courseId id of the course
     * @param session session of the viewer
     * @param postId the postId of the post to be viewed
     * @return true if the view count is successfully added, false otherwise
     */
    @PostMapping("{courseId}/post/{postId}/view")
    public boolean view(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestBody Session session) {
        String sessionId = session.getSessionId();
        //check session, is involved
        if (courseId.length() != 32 || sessionId.length() != 64 || postId == null) {
            log.info("Failed to add view: Invalid arguments");
            return false;
        }

        //check user is involved in course
        if (courseController.checkCourseMembership(courseId, sessionId) == null) {
            log.info("Failed to add view: Invalid session or user is not in the course");
            return false;
        }

        try {
            Optional<QuestionRoot> optionalQuestionRoot = questionRootRepository.findById(postId);
            if (!optionalQuestionRoot.isPresent()) {
                log.info("Failed to add view: Question root not found");
                return false;
            }
            QuestionRoot qr = optionalQuestionRoot.get();
            qr.addViewerId(session.getEmail());
            questionRootRepository.save(qr);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Likes a post.
     *
     * @param courseId id of the course
     * @param session session of the viewer
     * @param postId the postId of the post to be viewed
     * @param value 1 for like, -1 dislike, 0 undo like/dislike
     * @return true if the post is successfully liked, false otherwise
     */
    @PostMapping("{courseId}/post/{postId}/like")
    public boolean like(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestBody Session session,
            @RequestParam("value") String value
    ) {
        String sessionId = session.getSessionId();

        if (courseId.length() != 32
                || postId.length() != 32
                || sessionId == null
                || sessionId.length() != 64
                || value == null) {
            log.info("Failed to like post: Invalid arguments");
            return false;
        }
        int vote;
        try {
            vote = Integer.parseInt(value);
            if (vote < -1 || vote > 1) {
                log.info("Failed to like post: Invalid arguments");
                return false;
            }
        } catch (Exception e) {
            log.info("Failed to like post: Invalid arguments");
            return false;
        }

        //check user is involved in course
        if (courseController.checkCourseMembership(courseId, sessionId) == null) {
            log.info("Failed to like post: Invalid session or user is not in the course");
            return false;
        }

        //check post exists
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (!optionalPost.isPresent()) {
            log.info("Failed to like post: Post does not exists");
            return false;
        }

        //like the post
        String userId = sessionRepository.findById(sessionId).get().getEmail();
        Post post = optionalPost.get();
        post.like(userId, vote);
        postRepository.save(post);
        return true;
    }

    /**
     * Delete a post. Only instructor and the student poster can remove a post.
     *
     * @param courseId id of the course
     * @param session session of the viewer
     * @param postId the postId of the post to be viewed
     * @param postType type of the post "QUESTIONROOT", "FOLLOWUPQUESTION", etc.
     * @return true if the post is successfully deleted, false otherwise
     */
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

    /**
     * Check if the user is the poster of a post
     *
     * @param courseId id of the course
     * @param session session of the viewer
     * @param postId the postId of the post to be viewed
     * @return true if the user is the poster of a post, otherwise false
     */
    @PostMapping("{courseId}/post/{postId}/checkOwnership")
    public boolean checkPostOwnership(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestBody Session session) {

        //check arguments
        if (session == null || courseId.length() != 32 || postId.length() != 32) {
            log.info("Failed to check ownership: Invalid arguments");
            return false;
        }

        //validate session
        Optional<Session> optionalSession = sessionRepository.findById(session.getSessionId());
        if (!optionalSession.isPresent() || !optionalSession.get().getEmail().equals(session.getEmail())) {
            log.info("Failed to check ownership: Invalid session");
            return false;
        }

        //check post valid
        Post post = postRepository.getPostElseNull(postId);
        if (post == null || !post.getCourseId().equals(courseId)) {
            log.info("Failed to check ownership: Post not found or invalid");
            return false;
        }

        //check the requester is the post author
        if (!post.getAuthorUserId().equals(session.getEmail())) {
            log.info("Failed to check ownership: Unauthorized");
            return false;
        }
        return true;
    }


    @PostMapping("{courseId}/post/{postId}/edit")
    public boolean editPost(
            @PathVariable("courseId") String courseId,
            @PathVariable("postId") String postId,
            @RequestParam("sessionId") String sessionId,
            @RequestBody Post modifiedPost) {
        //Check session is valid, and user authorized to edit post
        Session session = sessionRepository.getSessionElseNull(sessionId);
        if (!checkPostOwnership(courseId, postId, session)) {
            log.info("Failed to edit post: Unauthorized user");
            return false;
        }

        //Check posts exists
        Post post = postRepository.getPostElseNull(postId);
        if (post == null) {
            log.info("Failed to edit post: Post does not exist");
            return false;
        }

        //Modify and save
        post.setContent(modifiedPost.getContent());
        postRepository.save(post);
        log.info("EDIT POST: " + post);
        return true;
    }
}
