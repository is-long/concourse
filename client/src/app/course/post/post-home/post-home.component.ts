import {Component, OnInit} from '@angular/core';
import {CourseService} from "../../../services/course.service";
import {Router} from "@angular/router";
import {Course} from "../../../shared/course";
import {QuestionRoot} from "../../../shared/post/question-root";
import {User} from "../../../shared/user/user";
import {UserService} from "../../../services/user.service";


@Component({
  selector: 'app-post-home',
  templateUrl: './post-home.component.html',
  styleUrls: ['./post-home.component.css']
})
export class PostHomeComponent implements OnInit {
  courseId: string;
  postId: string;
  course: Course;
  questionRoot: QuestionRoot;
  showAddAnswerEditor: boolean = false;
  kv = new Map();
  user: User;


  constructor(private router: Router, private courseService: CourseService, private userService: UserService) {
    let arr = this.router.url.split("/");
    this.postId = arr[arr.length - 1];
    this.courseId = arr[arr.length - 3];

    //add view to question root
    this.courseService.addViewer(this.courseId, this.postId).subscribe(
      () => {
        //then get course, and populate fields
        this.courseService.getCourse(this.courseId).subscribe(
          data => {
            this.course = data;
            for (let questionRoot of this.course.questionRootList) {
              if (questionRoot.id === this.postId) {
                this.questionRoot = questionRoot;
                break;
              }
            }
            if (this.questionRoot === null) {
              this.router.navigateByUrl('/course/' + this.courseId)
            }
            for (let qr of this.course.questionRootList) {
              for (let qra of qr.questionRootAnswerList) {
                this.kv.set(qra.id, false);
              }
              for (let fq of qr.followupQuestionList) {
                this.kv.set(fq.id, false);
              }
            }

            this.userService.getSelf().subscribe(
              data => {
                this.user = data;
              }
            );
          }
        );
      })
  }

  ngOnInit() {
  }

  toggleEditor(id: string) {
    this.kv.set(id, !this.kv.get(id));
  }

  getDate(time: number) {
    var options = {year: 'numeric', month: 'short', day: 'numeric'};
    return new Date(time).toLocaleDateString("en-US", options);
  }

  getDaysAgo(time: number) {
    let daysAgo = Math.floor((new Date().getTime() - time) / 86400000);
    if (daysAgo === 0) {
      return "Today"
    } else if (daysAgo === 1) {
      return "Yesterday"
    } else if (daysAgo < 30) {
      return daysAgo.toString() + " days ago"
    } else {
      return this.getDate(time);
    }
  }

  like(postId: string, value: number) {
    this.courseService.like(this.courseId, postId, value).subscribe(
      data => {
        window.location.reload();
      }
    );
  }
}

