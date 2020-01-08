import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {CourseService} from "../../services/course.service";
import {Course} from "../../shared/course";
import {faCoffee} from "@fortawesome/free-solid-svg-icons/faCoffee";
import {faArrowUp} from "@fortawesome/free-solid-svg-icons/faArrowUp";
import {faArrowDown} from "@fortawesome/free-solid-svg-icons/faArrowDown";


@Component({
  selector: 'app-course-home',
  templateUrl: './course-home.component.html',
  styleUrls: ['./course-home.component.css']
})
export class CourseHomeComponent implements OnInit {

  course: Course;
  faArrowUp = faArrowUp;
  faArrowDown = faArrowDown;

  constructor(private router: Router, private courseService: CourseService, private route: ActivatedRoute) {
    let arr = router.url.split("/");
    let courseId = arr[arr.length - 1];
    this.courseService.getCourse(courseId).subscribe(
      optionalCourse => {
        //if course exists
        if (optionalCourse != null){
          this.course = optionalCourse;
        } else {
          this.router.navigateByUrl('/dashboard');
        }
      }
    );
  }

  ngOnInit() {
  }

}
