import {Component, OnInit} from '@angular/core';
import {CourseService} from "../../services/course.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-join-course',
  templateUrl: './join-course.component.html',
  styleUrls: ['./join-course.component.css']
})
export class JoinCourseComponent implements OnInit {
  courseId: string;
  inviteId: string;
  isNotValid: boolean = false;

  constructor(private courseService: CourseService, private router: Router) {
  }

  ngOnInit() {
  }

  join() {
    this.courseService.joinCourse(this.courseId, sessionStorage.getItem('sessionId'), this.inviteId)
      .subscribe(
        data => {
          if (data) {
            this.router.navigateByUrl('/course/' + this.courseId + '/home');
          } else {
            this.isNotValid = true;
          }
        }
      );
  }
}
