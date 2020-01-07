import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";

@Component({
  selector: 'app-course-home',
  templateUrl: './course-home.component.html',
  styleUrls: ['./course-home.component.css']
})
export class CourseHomeComponent implements OnInit {

  constructor(private router: Router) {
  }

  ngOnInit() {
  }

}
