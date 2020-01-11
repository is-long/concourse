import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {CourseService} from "../../services/course.service";
import {Course} from "../../shared/course";

import {faEye} from "@fortawesome/free-solid-svg-icons/faEye";
import {faCommentAlt} from "@fortawesome/free-solid-svg-icons/faCommentAlt";
import {faCommentMedical} from "@fortawesome/free-solid-svg-icons/faCommentMedical";
import {QuestionRoot} from "../../shared/post/question-root";
import {forEachComment} from "tslint";


@Component({
  selector: 'app-course-home',
  templateUrl: './course-home.component.html',
  styleUrls: ['./course-home.component.css']
})
export class CourseHomeComponent implements OnInit {

  course: Course;
  courseId: string;
  url: string = "/post/new";
  // questionRootList: QuestionRoot[];
  name: string;
  showSearch: boolean = false;
  showFilter: boolean = false;


  questionRoothasInstructorAnswerMap: Map<string, boolean> = new Map();
  faCommentAlt = faCommentAlt;
  faEye = faEye;
  faCommentMedical = faCommentMedical;

  qrIdqraCountMap: Map<string, number> = new Map();
  qrIdfqCountMap: Map<string, number> = new Map();

  originalQuestionRootList: QuestionRoot[];
  isQuerying: boolean = false;


  constructor(private router: Router, private courseService: CourseService, private route: ActivatedRoute) {

  }


  ngOnInit() {
    let arr = this.router.url.split("/");
    let courseId = arr[arr.length - 1];
    this.courseService.getCourse(courseId).subscribe(
      optionalCourse => {
        //if course exists
        if (optionalCourse != null) {
          this.course = optionalCourse;
          this.name = optionalCourse.name;
          this.url = 'course/' + optionalCourse.id + '/post/new';
          this.originalQuestionRootList = optionalCourse.questionRootList;

          for (let qr of optionalCourse.questionRootList) {
            this.qrIdqraCountMap.set(qr.id, qr.questionRootAnswerList.length);
            this.qrIdfqCountMap.set(qr.id, qr.followupQuestionList.length);
            this.questionRoothasInstructorAnswerMap.set(qr.id, false);
            for (let qra of qr.questionRootAnswerList) {
              if (qra.authorType === "INSTRUCTOR") {
                this.questionRoothasInstructorAnswerMap.set(qr.id, true);
              }
            }
          }
        } else {
          this.router.navigateByUrl('/dashboard');
        }
      }
    );
  }


  containsAllString: string = "";
  containsExactString: string = "";
  containsAnyString: string = "";
  containsNoneString: string = "";

  containsAllArr: string[];
  containsExactArr: string[];
  containsAnyArr: string[];
  containsNoneArr: string[];

  stringToArray() {
    this.containsAllArr = [];
    this.containsExactArr = [];
    this.containsAnyArr = [];
    this.containsNoneArr = [];

    if (this.containsAllString != "") {
      this.containsAllArr = this.containsAllString.toLowerCase().split(",");
      this.containsAllArr.forEach(s => s.trim());
    }
    if (this.containsExactString != "") {
      this.containsExactArr = this.containsExactString.toLowerCase().split(",");
      this.containsExactArr.forEach(s => s.trim());
    }
    if (this.containsAnyString != "") {
      this.containsAnyArr = this.containsAnyString.toLowerCase().split(",");
      this.containsAnyArr.forEach(s => s.trim());
    }
    if (this.containsNoneString != "") {
      this.containsNoneArr = this.containsNoneString.toLowerCase().split(",");
      this.containsNoneArr.forEach(s => s.trim());
    }


  }

  extractContent(qr: QuestionRoot) {
    let s: string = "";
    s = s.concat(qr.content, qr.authorName);

    for (let qra of qr.questionRootAnswerList) {
      s = s.concat(qra.content, qra.authorName);
      for (let qrar of qra.questionRootAnswerReplyList) {
        s = s.concat(qra.content, qra.authorName);
      }
    }

    for (let fq of qr.followupQuestionList) {
      s = s.concat(fq.content, fq.authorName);
      for (let fa of fq.followupAnswerList) {
        s = s.concat(fa.content, fa.authorName);
      }
    }
    return s.toLowerCase();
  }

  checkPostMatchSearch(qr: QuestionRoot) {
    let postContent = this.extractContent(qr).toLowerCase();

    for (let keyword of this.containsAllArr) {
      if (!postContent.includes(keyword)) {
        return false;
      }
    }
    for (let keyword of this.containsExactArr) {
      if (!postContent.includes(keyword)) {
        return false;
      }
    }

    if (this.containsNoneArr.length === 1) {
      for (let keyword of this.containsNoneArr) {
        if (postContent.includes(keyword)) {
          return false;
        }
      }
    }

    if (this.containsAnyArr.length === 0) {
      return true
    }

    for (let keyword of this.containsAnyArr) {
      if (postContent.includes(keyword)) {
        return true;
      }
    }
    return false;
  }

  query() {
    this.clearArr();
    this.stringToArray();

    let result: QuestionRoot[] = [];

    for (let qr of this.course.questionRootList) {
      if (this.checkPostMatchSearch(qr)) {
        if (this.checkPostMatchFilter(qr)) {
          result.push(qr);
        }
      }
    }

    this.course.questionRootList = result;
    this.isQuerying = true;
  }


  filterAuthor: string;

  selectAuthor(author: string) {
    this.filterAuthor = author;
  }

  hasInstructorAnswer(qr: QuestionRoot){
    for (let qra of qr.questionRootAnswerList) {
      if (qra.authorType === "INSTRUCTOR") {
        return true;
      }
    }
    return false;
  }
  filterHasInstructorAnswer(qrArr : QuestionRoot[]){
    let result: QuestionRoot[] = [];
    for (let qr of qrArr){
      if (this.hasInstructorAnswer(qr)){
        result.push(qr);
      }
    }
    return result;
  }


  checkPostMatchFilter(qr: QuestionRoot) {
    let result: QuestionRoot[] = [];


    let filterTags = document.getElementsByName("filterTags");
    for (var i = 0; i < filterTags.length; i++){
      if (filterTags[i]['checked'] === true){
        if (filterTags[i]['value'] === "all"){
          break;
        } else {
          break;
        }
      }
    }

    let filterFolders = document.getElementsByName("filterFolders");
    for (var i = 0; i < filterFolders.length; i++){
      if (filterFolders[i]['checked'] === true){



      }
    }


    let filterDate = document.getElementsByName("filterDate");
    for (var i = 0; i < filterDate.length; i++){
      if (filterDate[i]['checked'] === true){



      }
    }

    return true;
  }


  clearQuery() {
    this.course.questionRootList = this.originalQuestionRootList;
    this.isQuerying = false;
  }

  clearArr() {
    this.containsAllArr = null;
    this.containsExactArr = null;
    this.containsAnyArr = null;
    this.containsNoneArr = null;
  }
}
