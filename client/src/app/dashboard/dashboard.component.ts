import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthService} from "../services/auth.service";
import {Router} from "@angular/router";
import {UserService} from "../services/user.service";
import {Course} from "../shared/course";
import {CourseService} from "../services/course.service";

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
          let courseInstructedIds: string[] = data['courseInstructedIds'];
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


}
