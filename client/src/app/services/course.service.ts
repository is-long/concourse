import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Course} from "../shared/course";
import {AuthService} from "./auth.service";
import {environment} from "../../environments/environment.prod";
import {Session} from "../shared/session";

@Injectable({
  providedIn: 'root'
})
export class CourseService {

  private url: string = environment.apiUrl;

  constructor(private http: HttpClient, private authService: AuthService) { }

  addCourse(course: Course){
    let session: Session = this.authService.getSession();
    return this.http.post<Course>(this.url + "/course/new/" + session.sessionId, course);
  }

  checkMember(courseId: string){
    return this.http.get<string>(this.url + "/course/" + courseId + "/checkmember/" + this.authService.getSession().sessionId);
  }

  joinCourse(courseId: string, sessionId: string, inviteId: string){
    let base_url = this.url + "/course/" + courseId + "/join";
    let param = "?sessionId=" + sessionId + "&inviteId=" + inviteId;
    return this.http.get(base_url + param);
  }
}
