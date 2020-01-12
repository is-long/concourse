import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

import {Course} from "../shared/course";
import {AuthService} from "./auth.service";
import {environment} from "../../environments/environment.prod";
import {Session} from "../shared/session";
import {QuestionRoot} from "../shared/post/question-root";
import {QuestionRootAnswer} from "../shared/post/question-root-answer";
import {QuestionRootAnswerReply} from "../shared/post/question-root-answer-reply";
import {FollowupQuestion} from "../shared/post/followup-question";
import {FollowupAnswer} from "../shared/post/followup-answer";
import {Post} from "../shared/post/post";

@Injectable({
  providedIn: 'root'
})
export class CourseService {

  private url: string = environment.apiUrl;

  constructor(private http: HttpClient, private authService: AuthService) {
  }


  getCourse(courseId: string) {
    return this.http.post<Course>(this.url + "/course/" + courseId + "/get", this.authService.getSession());
  }

  addCourse(course: Course) {
    let session: Session = this.authService.getSession();
    return this.http.post<Course>(this.url + "/course/new/" + session.sessionId, course);
  }

  checkMember(courseId: string) {
    return this.http.get<string>(this.url + "/course/" + courseId + "/checkmember/" + this.authService.getSession().sessionId);
  }

  joinCourse(courseId: string, sessionId: string, inviteId: string) {
    let base_url = this.url + "/course/" + courseId + "/join";
    let param = "?sessionId=" + sessionId + "&inviteId=" + inviteId;
    return this.http.get(base_url + param);
  }

  addQuestionRoot(questionRoot: QuestionRoot) {
    return this.http.post<QuestionRoot>(this.url + "/course/" + questionRoot.courseId +
      "/post/new/questionroot?sessionId=" + this.authService.getSession().sessionId,
      questionRoot);
  }

  addQuestionRootAnswer(questionRootAnswer: QuestionRootAnswer) {
    return this.http.post<QuestionRootAnswer>(this.url + "/course/" + questionRootAnswer.courseId
      + "/post/new/questionrootanswer?sessionId=" + this.authService.getSession().sessionId, questionRootAnswer);
  }

  addQuestionRootAnswerReply(questionRootAnswerReply: QuestionRootAnswerReply) {
    return this.http.post<QuestionRootAnswerReply>(this.url + "/course/" + questionRootAnswerReply.courseId
      + "/post/new/questionrootanswerreply?sessionId=" + this.authService.getSession().sessionId, questionRootAnswerReply);
  }

  addFollowupQuestion(followupQuestion: FollowupQuestion) {
    return this.http.post<FollowupQuestion>(this.url + "/course/" + followupQuestion.courseId
      + "/post/new/followupquestion?sessionId=" + this.authService.getSession().sessionId, followupQuestion);
  }

  addFollowupAnswer(followupAnswer: FollowupAnswer) {
    return this.http.post<FollowupAnswer>(this.url + "/course/" + followupAnswer.courseId
      + "/post/new/followupanswer?sessionId=" + this.authService.getSession().sessionId, followupAnswer);
  }


  like(courseId: string, postId: string, value: number) {
    return this.http.post<boolean>(
      this.url + "/course/" + courseId + "/post/" + postId + "/like?value=" + value,
      this.authService.getSession()
    );
  }

  addViewer(courseId: string, postId: string) {
    return this.http.post(this.url + "/course/" + courseId + "/post/" + postId + "/view",
      this.authService.getSession());
  }

  getCourseFolders(courseId: string) {
    return this.http.post<string[]>(this.url + "/course/" + courseId + "/getfolders", this.authService.getSession());
  }

  addCourseFolders(courseId: string, newFolders: string[]) {
    return this.http.post(this.url + "/course/" + courseId + "/addfolders?sessionId=" +
      this.authService.getSession().sessionId, newFolders);
  }


  deletePost(courseId: string, postId: string, postType: string) {
    return this.http.post(this.url + "/course/" + courseId + "/post/" + postId + "/delete?postType=" + postType,
      this.authService.getSession());
  }

  isOwnPost(courseId: string, postId: string) {
    return this.http.post(this.url + "/course/" + courseId + "/post/" + postId + "/checkOwnership",
      this.authService.getSession());
  }

  getPost(courseId: string, postId: string) {
    return this.http.post<any>(this.url + "/course/" + courseId + "/post/" + postId + "/get",
      this.authService.getSession());
  }

  editPost(courseId: string, postId: string, post: Post) {
    return this.http.post<any>(this.url + "/course/" + courseId + "/post/" + postId
      + "/edit?sessionId=" + this.authService.getSession().sessionId, post
    );
  }

  sendInvitation(courseId: string, emails: string[], role: string) {
    return this.http.post(this.url + "/course/" + courseId + '/invite?sessionId='
      + this.authService.getSession().sessionId + '&role=' + role, emails);
  }

  removeStudents(courseId: string, emails: string[]) {
    return this.http.post(this.url + "/course/" + courseId + '/delete/students?sessionId='
      + this.authService.getSession().sessionId, emails);
  }

  deleteCourse(courseId: string) {
    return this.http.post(this.url + "/course/" + courseId + '/delete/course',
      this.authService.getSession());
  }
}

