import {Component, OnInit} from '@angular/core';
import {Course} from "../../shared/course";
import {CourseService} from "../../services/course.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-create-course',
  templateUrl: './create-course.component.html',
  styleUrls: ['./create-course.component.css']
})
export class CreateCourseComponent implements OnInit {

  name: string;
  description: string;
  instructorEmailString: string;
  studentEmailString: string;

  constructor(private courseService: CourseService, private router:Router) {}

  ngOnInit() { }

  onSubmit() {
    let course: Course = new Course();
    course.name = this.name;
    course.description = this.description;
    course.creatorInstructorId = localStorage.getItem('email');
    course.instructorIds = this.emailStringToEmailArray(this.instructorEmailString);
    course.instructorIds.push(course.creatorInstructorId);  //add creator
    course.studentIds =  this.emailStringToEmailArray(this.studentEmailString);
    course.questionRootList = [];

    //create course and send invitation emails
    this.courseService.addCourse(course).subscribe(
      data => {
        //get back the created Course
        if (data != null){
          //go to course home
          this.router.navigateByUrl('/course/' + data.id  + '/home');
        } else {
          this.router.navigateByUrl('/course/new');
        }
      }
    );
  }

  emailStringToEmailArray(arr: string): string[] {
    if (arr == null || arr.length == 0){
      return [];
    }

    let emailArr = arr.split(",");
    for (let [index, val] of emailArr.entries()) {
      let email = val.trim();
      if (!this.isValidEmail(email)) {
        return null;
      }
      emailArr[index] = email;
    }
    return emailArr;
  }

  //TODO:
  isValidEmail(email: string): boolean {
    return true;
  }
}
