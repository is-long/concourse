import {Component, OnInit} from '@angular/core';
import {CourseService} from "../../../services/course.service";
import {Router} from "@angular/router";
import {Course} from "../../../shared/course";
import {QuestionRoot} from "../../../shared/post/question-root";
import {User} from "../../../shared/user/user";
import {UserService} from "../../../services/user.service";
import {QuestionRootAnswer} from "../../../shared/post/question-root-answer";
import {faEllipsisH} from "@fortawesome/free-solid-svg-icons";
import {Post} from "../../../shared/post/post";


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
  faEllipsisH = faEllipsisH;


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
            if (this.questionRoot === undefined) {
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

            this.sortBy('new');

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

  instructorUpvoteCount(post: Post){
    let result: number = 0;
    for (let instructorId of this.course.instructorIds){
      let pref: number = post.likesUserIDMap[instructorId];
      if (pref != null && pref === 1){
        result++;
      }
    }
    return result;
  }




  editAnswer(postId: string){
    this.router.navigateByUrl('/course/' + this.courseId + '/post/' + postId + '/edit')
  }

  deletePost(postId: string, postType: string){
    this.courseService.deletePost(this.courseId, postId, postType).subscribe(
      data => {
        window.location.reload();
      }
    );
  }

  reportAnswer(qraId: string, postType: string){

  }










  sortBy(selection:string){
    //most discussion first
    if (selection === "discussion"){
      this.questionRoot.questionRootAnswerList.sort((qra1: QuestionRootAnswer, qra2: QuestionRootAnswer) => {
        return qra2.questionRootAnswerReplyList.length - qra1.questionRootAnswerReplyList.length
      });
    }

    //newest first
    if (selection === "new"){
      this.questionRoot.questionRootAnswerList.sort((qra1: QuestionRootAnswer, qra2: QuestionRootAnswer) => {
        return qra2.postDate - qra1.postDate
      });
    }

    //oldest first
    if (selection === "old"){
      this.questionRoot.questionRootAnswerList.sort((qra1: QuestionRootAnswer, qra2: QuestionRootAnswer) => {
        return qra1.postDate - qra2.postDate
      });
    }

    //most view first
    if (selection === "vote"){
      this.questionRoot.questionRootAnswerList.sort((qra1: QuestionRootAnswer, qra2: QuestionRootAnswer) => {
        return qra2.likeCount - qra1.likeCount
      });
    }
  }



  toggleEditor(id: string) {
    this.kv.set(id, !this.kv.get(id));
  }

  getDate(time: number) {
    let daysAgo = Math.floor((new Date().getTime() - time) / 86400000);
    if (daysAgo === 0) {
      if (new Date().getTime() - time < 60 * 60000){
        return Math.floor((new Date().getTime() - time) / 60000) +  " minutes ago"
      } else if (new Date().getTime() - time < 24 * 60 * 60000){
        return Math.floor((new Date().getTime() - time) / (60 * 60000)) +  " hours ago"
      }
      return "Today"
    } else if (daysAgo === 1) {
      return "Yesterday"
    } else if (daysAgo < 30) {
      return daysAgo.toString() + " days ago"
    } else {
      return new Date(time).toLocaleDateString("en-US", {year: 'numeric', month: 'short', day: 'numeric'});
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

