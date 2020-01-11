import { Component, OnInit } from '@angular/core';
import {CourseService} from "../../services/course.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-course-settings',
  templateUrl: './course-settings.component.html',
  styleUrls: ['./course-settings.component.css']
})
export class CourseSettingsComponent implements OnInit {

  folders: string[];
  newFolderString: string;
  newFolderSubmitted: boolean = false;
  courseId: string;

  constructor(private courseService: CourseService, private router: Router) {
    let arr = router.url.split("/");
    this.courseId = arr[arr.length - 2];

    this.courseService.getCourseFolders(this.courseId).subscribe(
      data => {
        this.folders = data;
      }
    );
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
}
