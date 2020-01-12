package com.concourse.tools;

import com.concourse.models.Course;
import com.concourse.models.posts.*;
import com.concourse.models.users.Instructor;
import com.concourse.models.users.Student;
import com.concourse.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FillerData {

    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private InstructorRepository instructorRepository;
    private StudentRepository studentRepository;
    private PostRepository postRepository;

    public FillerData(UserRepository userRepository, CourseRepository courseRepository, InstructorRepository instructorRepository, StudentRepository studentRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
        this.studentRepository = studentRepository;
        this.postRepository = postRepository;

        //create course
        Course c = new Course();
        c.setName("CIS 455");
        c.setDescription("Web");
        List<String> folders = new ArrayList<>();
        folders.add("General");
        c.setFolders(folders);

        //create instructor and set as instructor of the course
        Instructor i = new Instructor();
        i.setEmail("islong@seas.upenn.edu");
        i.setEmailConfirmed(true);
        i.setRole("INSTRUCTOR");
        i.setName("Isaac");
        i.addCourseInstructedIds(c.getId());
        this.instructorRepository.save(i);

        //create student and enroll in course
        Student s = new Student();
        s.setEmailConfirmed(true);
        s.setEmail("john@asdflkajsdflkj.com");
        s.setName("Johnny");
        s.setRole("STUDENT");
        s.addCourseEnrolledIds(c.getId());
        this.studentRepository.save(s);

        //create student and enroll in course
        Student s2 = new Student();
        s2.setEmailConfirmed(true);
        s2.setEmail("abcd@icloud.com");
        s2.setName("Isaac 2");
        s2.setRole("STUDENT");
        s2.addCourseEnrolledIds(c.getId());
        this.studentRepository.save(s2);


        //add i as one of the instructor, and as creator of the course
        c.addInstructor(i.getEmail());
        c.setCreatorInstructorId(i.getEmail());
        //add s as student of the course
        c.addStudent(s.getEmail());
        c.addStudent(s2.getEmail());

        //create question root
        String qrContent =
                "";
        QuestionRoot qr = new QuestionRoot(c.getId(),
                qrContent,
                s, "How does the “final” keyword in Java work? (I can still modify an object.)"
        );

        QuestionRootAnswer qra = new QuestionRootAnswer(c.getId(),
                "<p><em>Final</em>&nbsp;keyword has a numerous way to use:</p><ul><li>A final&nbsp;<strong>class</strong>&nbsp;cannot be subclassed.</li><li>A final&nbsp;<strong>method</strong>&nbsp;cannot be overridden by subclasses</li><li>A final&nbsp;<strong>variable</strong>&nbsp;can only be initialized once</li></ul><p>Other usage:</p><ul><li><em>When an anonymous inner class is defined within the body of a method, all variables declared final in the scope of that method are accessible from within the inner class</em></li></ul><p>A static class variable will exist from the start of the JVM, and should be initialized in the class. The error message won't appear if you do this.</p>"
                , i, qr.getId());
        QuestionRootAnswerReply qrar1 = new QuestionRootAnswerReply(c.getId(), "This is by far my favorite answer. Simple and straight-forward, this is what I would expect to read in online docs about java.", s, qra.getId());
        QuestionRootAnswerReply qrar2 = new QuestionRootAnswerReply(c.getId(), "So in static variables we can initialize as many times as we want?", s, qra.getId());
        this.postRepository.save(qrar1);
        this.postRepository.save(qrar2);
        qra.addQuestionRootAnswerReply(qrar1);
        qra.addQuestionRootAnswerReply(qrar2);
        this.postRepository.save(qra);
        qr.addQuestionRootAnswer(qra);

        //create followup question
        FollowupQuestion fq = new FollowupQuestion(c.getId(), "<h2> How about 2 + 2? </h2>", s, qr.getId());
        FollowupAnswer fa1 = new FollowupAnswer(c.getId(), "<h2> It's 4. </h2>", i, fq.getId());
        FollowupAnswer fa2 = new FollowupAnswer(c.getId(), "<h2> Thanks !!!</h2>", s, fq.getId());
        FollowupAnswer fa3 = new FollowupAnswer(c.getId(), "<h2> Welcome !!!</h2>", i, fq.getId());
        this.postRepository.save(fa1);
        this.postRepository.save(fa2);
        this.postRepository.save(fa3);
        fq.addFollowupAnswer(fa1);
        fq.addFollowupAnswer(fa2);
        fq.addFollowupAnswer(fa3);
        this.postRepository.save(fq);
        qr.addFollowupQuestion(fq);
        qr.setHasInstructorAnswer();
        qr.setFolder("General");

        this.postRepository.save(qr);
        c.addQuestionRoot(qr);
        this.courseRepository.save(c);

    }
}
