import { Component, OnInit } from '@angular/core';
import {CourseService} from "../../services/course.service";
import {Router} from "@angular/router";
import {Course} from "../../shared/course";

@Component({
  selector: 'app-course-settings',
  templateUrl: './course-settings.component.html',
  styleUrls: ['./course-settings.component.css']
})
export class CourseSettingsComponent implements OnInit {

  newStudentsEmailString: string;
  newInstructorsEmailString: string;

  folders: string[];
  newFolderString: string;
  newFolderSubmitted: boolean = false;
  courseId: string;
  course: Course;

  showInviteInstructor: boolean = false;
  showInviteStudent: boolean = false;
  showQuestionFolder: boolean = false;

  constructor(private courseService: CourseService, private router: Router) {
    let arr = router.url.split("/");
    this.courseId = arr[arr.length - 2];

    this.courseService.getCourse(this.courseId).subscribe(
      data => {
        this.course = data;

        this.courseService.getCourseFolders(this.courseId).subscribe(
          data => {
            this.folders = data;
          }
        );
      }
    )
  }

  ngOnInit() {
  }


  addFolders(){
    this.courseService.addCourseFolders(this.courseId, this.newFolderString.split(",")).subscribe(
      data => {
        window.location.reload();
      }
    );
  }


  addStudents(){
    let studentEmailArr = this.emailStringToEmailArray(this.newStudentsEmailString);
    this.courseService.sendInvitation(this.courseId, studentEmailArr, 'STUDENT').subscribe(
      () => {
        window.location.reload();
      }
    );
  }

  addInstructors(){
    let instructorEmailArr = this.emailStringToEmailArray(this.newInstructorsEmailString);
    this.courseService.sendInvitation(this.courseId, instructorEmailArr, 'INSTRUCTOR').subscribe(
      () => {
        window.location.reload();
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
