import {Component, Input, OnInit} from '@angular/core';
import {CourseService} from "../../../services/course.service";
import {QuestionRootAnswer} from "../../../shared/post/question-root-answer";
import {UserService} from "../../../services/user.service";
import {User} from "../../../shared/user/user";
import {QuestionRootAnswerReply} from "../../../shared/post/question-root-answer-reply";
import {FollowupQuestion} from "../../../shared/post/followup-question";
import {FollowupAnswer} from "../../../shared/post/followup-answer";
import {Router} from "@angular/router";

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.css']
})
export class EditorComponent implements OnInit {

  @Input() parentId: string;
  @Input() type: string;
  @Input() courseId: string;
  @Input() questionRootId: string;
  content: string;
  display: boolean = true;

  constructor(private courseService: CourseService, private userService: UserService, private router: Router) {
  }

  ngOnInit() {
  }

  onSubmit() {
    let user: User;
    this.userService.getSelf().subscribe(
      data => {
        if (data != null) {
          user = data;

          if (this.type === "QUESTIONROOTANSWER") {
            let questionRootAnswer: QuestionRootAnswer = new QuestionRootAnswer();
            questionRootAnswer.questionRootId = this.questionRootId;
            questionRootAnswer.content = this.content;
            questionRootAnswer.questionRootAnswerReplyList = [];
            questionRootAnswer.courseId = this.courseId;
            questionRootAnswer.authorUserId = user.email;
            questionRootAnswer.authorName = user.name;
            questionRootAnswer.authorType = user.role;
            this.courseService.addQuestionRootAnswer(questionRootAnswer).subscribe(
              success => {
                window.location.reload();
              }
            );
          } else if (this.type === "QUESTIONROOTANSWERREPLY") {
            let questionRootAnswerReply: QuestionRootAnswerReply = new QuestionRootAnswerReply();
            questionRootAnswerReply.questionRootAnswerId = this.parentId;
            questionRootAnswerReply.content = this.content;
            questionRootAnswerReply.authorName = user.name;
            questionRootAnswerReply.authorType = user.role;
            questionRootAnswerReply.authorUserId = user.email;
            questionRootAnswerReply.courseId = this.courseId;
            this.courseService.addQuestionRootAnswerReply(questionRootAnswerReply).subscribe(
              success => {
                window.location.reload();
              }
            );
          } else if (this.type === "FOLLOWUPQUESTION") {
            let followupQuestion: FollowupQuestion = new FollowupQuestion();
            followupQuestion.content = this.content;
            followupQuestion.questionRootId = this.questionRootId;
            followupQuestion.courseId = this.courseId;
            followupQuestion.authorName = user.name;
            followupQuestion.authorUserId = user.email;
            followupQuestion.authorType = user.role;
            followupQuestion.followupAnswerList = [];
            this.courseService.addFollowupQuestion(followupQuestion).subscribe(
              success => {
                window.location.reload();
              }
            );
          } else if (this.type === "FOLLOWUPANSWER") {
            let followupAnswer: FollowupAnswer = new FollowupAnswer();
            followupAnswer.content = this.content;
            followupAnswer.courseId = this.courseId;
            followupAnswer.followupQuestionId = this.parentId;
            followupAnswer.authorName = user.name;
            followupAnswer.authorUserId = user.email;
            followupAnswer.authorType = user.role;
            this.courseService.addFollowupAnswer(followupAnswer).subscribe(
              success => {
                window.location.reload();
              }
            );
          } else {
            window.location.reload();
          }
        }
      }
    );
  }
}
