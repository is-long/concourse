import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {User} from "../shared/user/user";
import {environment} from "../../environments/environment.prod";
import {Student} from "../shared/user/student";
import {InstructorService} from "./instructor.service";
import {Instructor} from "../shared/user/instructor";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private url: string = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private router: Router
  ) { }

  confirmRegistration(confirmationId: string){
    return this.http.get(this.url + "/user/registration/confirm/" + confirmationId);
  }

  isRegistered(user: User){
    return this.http.post<boolean>(this.url + "/user/registration/check", user);
  }

  addStudent(student: Student){
    return this.http.post<Student>(this.url + "/user/registration/new/student", student);
  }
  addInstructor(instructor : Instructor){
    return this.http.post<Instructor>(this.url + "/user/registration/new/instructor", instructor);
  }
}
