import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthService} from "../services/auth.service";
import {Router} from "@angular/router";
import {UserService} from "../services/user.service";
import {Course} from "../shared/course";
import {CourseService} from "../services/course.service";
import {Post} from "../shared/post/post";
import {QuestionRoot} from "../shared/post/question-root";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {

  email;
  user: any;
  role: string;
  isInstructor: boolean;
  courses: Course[] = [];

  constructor(private authService: AuthService, private userService: UserService, private router: Router,
              private courseService: CourseService) {
    //check if instructor
    this.userService.getSelf().subscribe(
      data => {
        this.user = data;

        //if an instructor, get all the courseInstructed
        if (data.role === "INSTRUCTOR") {
          this.isInstructor = true;
          this.role = "INSTRUCTOR";
          let courseInstructedIds: string[] = data['courseInstructedIds'];
          if (courseInstructedIds != undefined && courseInstructedIds.length != 0) {
            for (let courseId of courseInstructedIds) {
              this.courseService.getCourse(courseId).subscribe(
                course => {
                  console.log('course is ');
                  console.log(course);
                  if (course != null) {
                    this.courses.push(<Course>course)
                  }
                }
              );
            }
          }
        }
        //else is a student, get all the courseEnrolled
        else {
          this.isInstructor = false;
          this.role = "STUDENT";
          let courseEnrolledIds: string[] = data['courseEnrolledIds'];
          if (courseEnrolledIds != undefined && courseEnrolledIds.length != 0) {
            for (let courseId of courseEnrolledIds) {
              this.courseService.getCourse(courseId).subscribe(
                course => {
                  if (course != null) {
                    this.courses.push(<Course>course)
                  }
                }
              );
            }
          }
        }
      }
    );
  }

  ngOnInit() {
    this.email = localStorage.getItem('email');
  }

  ngOnDestroy(): void {
  }


  getUnreadPostCount(course: Course){
    let userId: string = this.user.email;
    let count: number = 0;
    for (let qr of course.questionRootList){
      if (!qr.viewerIds.includes(userId)){
        count++;
      }
    }
    return count;
  }

  isGoodQuestion(){

  }

  isMarkedGood(post: Post, instructorIds: string[]) {
    for (let instructorId of instructorIds){
      if (post.likesUserIDMap[instructorId] != null){
        return true;
      }
    }
    return false;
  }

  getNewAndGoodQuestionCount(course: Course){
    let count: number = 0;
    for (let qr of course.questionRootList){
      if (this.isMarkedGood(qr, course.instructorIds)
      && !qr.viewerIds.includes(this.user.email)){
        count++;
      }
    }
    return count;
  }

  getNewInstructorPostCount(course: Course){
    let count: number = 0;
    for (let qr of course.questionRootList){
      if (qr.authorType === 'INSTRUCTOR'
        && !qr.viewerIds.includes(this.user.email)){
        count++;
      }
    }
    return count;
  }

  getNewAndGoodFollowupQuestionCount(course: Course){
    let count: number = 0;
    for (let qr of course.questionRootList){
      if (!qr.viewerIds.includes(this.user.email)){
        for (let fq of qr.followupQuestionList){
          if (this.isMarkedGood(fq, course.instructorIds)){
            count++;
          }
        }
      }
    }
    return count;
  }

  getHasNoAnswerCount(course: Course){
    let count: number = 0;
    for (let qr of course.questionRootList){
      if (qr.questionRootAnswerList.length === 0){
        count++;
      }
    }
    return count;
  }

  getUnresolvedFollowupCount(course: Course){
    let count: number = 0;
    for (let qr of course.questionRootList){
      for (let fq of qr.followupQuestionList){
        if (fq.followupAnswerList.length === 0){
          count++;
        }
      }
    }
    return count;
  }
}
