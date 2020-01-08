import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {User} from "../shared/user/user";
import {environment} from "../../environments/environment.prod";
import {Student} from "../shared/user/student";
import {Instructor} from "../shared/user/instructor";
import {AuthService} from "./auth.service";


@Injectable({
  providedIn: 'root'
})
export class UserService {

  private url: string = environment.apiUrl;

  constructor(
    private http: HttpClient, private router: Router, private authService: AuthService
  ) {
    this.url = this.url.replace("https", "http");
  }

  confirmRegistration(confirmationId: string){
    return this.http.get(this.url + "/user/registration/confirm/" + confirmationId);
  }

  getSelf(){
    return this.http.post<User>(this.url + "/user/self", this.authService.getSession())
  }

  isRegistered(user: User){
    this.url = this.url.replace("https", "http");
    return this.http.post<boolean>(this.url + "/user/registration/check", user);
  }

  addStudent(student: Student){
    return this.http.post<Student>(this.url + "/user/registration/new/student", student);
  }
  addInstructor(instructor : Instructor){
    return this.http.post<Instructor>(this.url + "/user/registration/new/instructor", instructor);
  }
}
