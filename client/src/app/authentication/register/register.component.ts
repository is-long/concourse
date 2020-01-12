import {Component, OnInit} from '@angular/core';
import {InstructorService} from "../../services/instructor.service";
import {UserService} from "../../services/user.service";
import {User} from "../../shared/user/user";
import {Student} from "../../shared/user/student";
import {Router} from "@angular/router";
import {Instructor} from "../../shared/user/instructor";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  email: string;
  role: string;
  name: string;

  isRegistered: boolean = false;
  isFormCorrect: boolean = true;

  finishedRegistration: boolean = false;
  failedRegistration: boolean = false;

  constructor(private userService: UserService, private router: Router) {
  }

  ngOnInit() {
  }

  setRole(role) {
    this.role = role;
  }

  register() {
    if (!this.checkForm()) {
      this.isFormCorrect = false;
    } else {
      this.isFormCorrect = true;

      let user: User = new User();
      user.email = this.email;
      user.name = this.name;
      user.role = this.role;


      this.userService.isRegistered(user).subscribe(
        isRegistered => {
          if (isRegistered) {
            this.isRegistered = true;
          } else {
            if (this.role === "STUDENT") {
              let student: Student = new Student();
              student.name = user.name;
              student.email = user.email;
              student.role = "STUDENT";
              student.courseEnrolledIds = [];

              this.userService.addStudent(student).subscribe(
                data => {
                  if (data != null){
                    this.finishedRegistration = true;
                  } else {
                    this.failedRegistration = true;
                  }
                }
              );
            } else if (this.role === "INSTRUCTOR") {
              let instructor: Instructor = new Instructor();
              instructor.name = user.name;
              instructor.email = user.email;
              instructor.role = "INSTRUCTOR";
              instructor.courseInstructed = [];

              this.userService.addInstructor(instructor).subscribe(
                data => {
                  if (data != null){
                    this.finishedRegistration = true;
                  } else {
                    this.failedRegistration = true;
                  }
                }
              );
            } else {
              this.failedRegistration = true;
            }
          }
        }
      );
    }
  }

  checkForm() {
    if (this.role != "STUDENT" && this.role != "INSTRUCTOR") {
      return false;
    }
    if (this.name === null  || this.name === undefined || this.name.length === 0) {
      return false;
    }

    //TODO: VALIDATE EMAIL
    if (this.email === null  || this.email === undefined ||  this.email.length === 0 || !this.email.includes("@")) {
      return false;
    }
    return true;
  }
}
