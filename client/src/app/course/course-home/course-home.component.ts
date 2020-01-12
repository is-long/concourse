import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {CourseService} from "../../services/course.service";
import {Course} from "../../shared/course";

import {faEye} from "@fortawesome/free-solid-svg-icons/faEye";
import {faCommentAlt} from "@fortawesome/free-solid-svg-icons/faCommentAlt";
import {faCommentMedical} from "@fortawesome/free-solid-svg-icons/faCommentMedical";
import {QuestionRoot} from "../../shared/post/question-root";
import {forEachComment} from "tslint";
import {User} from "../../shared/user/user";
import {UserService} from "../../services/user.service";
import {Post} from "../../shared/post/post";


@Component({
  selector: 'app-course-home',
  templateUrl: './course-home.component.html',
  styleUrls: ['./course-home.component.css']
})
export class CourseHomeComponent implements OnInit {

  user: User;
  course: Course;
  originalQuestionRootList: QuestionRoot[];

  //ICONS
  faCommentAlt = faCommentAlt;
  faEye = faEye;
  faCommentMedical = faCommentMedical;

  //STATES
  isQuerying: boolean = false;
  isViewCompact: boolean = false;
  showSearch: boolean = false;
  showFilter: boolean = false;

  //SEARCH FIELDS
  containsAllString: string = "";
  containsExactString: string = "";
  containsAnyString: string = "";
  containsNoneString: string = "";
  containsAllArr: string[];
  containsExactArr: string[];
  containsAnyArr: string[];
  containsNoneArr: string[];


  filterAuthorSelection: string;


  constructor(private router: Router, private courseService: CourseService, private route: ActivatedRoute,
              private userService: UserService) {
    let arr = this.router.url.split("/");
    let courseId = arr[arr.length - 1];

    this.courseService.getCourse(courseId).subscribe(optionalCourse => {
        if (optionalCourse != null) {
          this.course = optionalCourse;
          this.originalQuestionRootList = optionalCourse.questionRootList;

          this.userService.getSelf().subscribe(
            data => {
              this.user = data;

              //sort post by new
              this.sortBy('new');
            }
          );
        } else {
          this.router.navigateByUrl('/dashboard');
        }
      }
    );
  }

  ngOnInit() {
  }

  toggleView(selection: string) {
    if (selection === 'default') {
      this.isViewCompact = false;
    }
    if (selection === 'compact') {
      this.isViewCompact = true;
    }
  }

  //=============================
  // POST ATTRIBUTE
  //=============================

  hasInstructorAnswer(qr: QuestionRoot) {
    for (let qra of qr.questionRootAnswerList) {
      if (qra.authorType === "INSTRUCTOR") {
        return true;
      }
    }
    return false;
  }

  hasUnresolvedFollowups(qr: QuestionRoot) {
    for (let fq of qr.followupQuestionList) {
      if (fq.followupAnswerList.length === 0) {
        return true;
      }
    }
    return false;
  }

  isMarkedAGoodQuestion(qr: QuestionRoot) {
    for (let instructorId of this.course.instructorIds) {
      if (qr.likesUserIDMap[instructorId] != null) {
        return true;
      }
    }
    return false;
  }

  hasGreatFollowup(qr: QuestionRoot) {
    for (let instructorId of this.course.instructorIds) {
      for (let fq of qr.followupQuestionList) {
        if (fq.likesUserIDMap[instructorId]) {
          return true;
        }
      }
    }
    return false;
  }

  hasInstructorEndorsedAnswer(qr: QuestionRoot) {
    for (let instructorId of this.course.instructorIds) {
      for (let qra of qr.questionRootAnswerList) {
        if (qra.likesUserIDMap[instructorId]) {
          return true;
        }
      }
    }
    return false;
  }

  //=============================
  // SEARCH, FILTER, SORT
  //=============================

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

  filterByTag(qr: QuestionRoot) {
    let filterTags: string[] = this.extractCheckboxValue("filterTags");
    let result: QuestionRoot[] = [];

    if (filterTags.includes("all")) {
      return true;
    }

    if (filterTags.includes("unread")) {
      if (qr.viewerIds.includes(this.user.email)) {
        return false;
      }
    }

    if (filterTags.includes("hasUnresolvedFollowups")) {
      if (!this.hasUnresolvedFollowups(qr)) {
        return false;
      }
    }
    if (filterTags.includes("noInstructorAnswer")) {
      if (this.hasInstructorAnswer(qr)) {
        return false;
      }
    }
    if (filterTags.includes("noAnswerAtAll")) {
      if (qr.questionRootAnswerList.length != 0) {
        return false;
      }
    }
    if (filterTags.includes("greatQuestionFollowup")) {
      if (!this.isMarkedAGoodQuestion(qr) && !this.hasGreatFollowup(qr)) {
        return false;
      }
    }
    if (filterTags.includes("instructorOrEndorsed")) {
      if (!(this.hasInstructorAnswer(qr)) && !this.hasInstructorEndorsedAnswer(qr)) {
        return false;
      }
    }

    return true;
  }

  filterByFolder(qr: QuestionRoot) {
    let filterFolders: string[] = this.extractCheckboxValue("filterFolders");
    if (filterFolders.includes("all")) {
      return true;
    }
    if (filterFolders.includes(qr.folder)) {
      return true;
    }
    return false;
  }

  filterByDate(qr: QuestionRoot) {
    //TODO: fix implementation

    let now = new Date().getTime();
    let filterDates: string[] = this.extractCheckboxValue("filterDates");
    if (filterDates.includes("all")) {
      return true;
    }

    if (filterDates.includes("last24hrs")) {
      if (qr.postDate < now - 86400000) {
        return false;
      }
    }

    if (filterDates.includes("last3days")) {
      if (qr.postDate < now - 86400000 * 3) {
        return false;
      }
    }

    if (filterDates.includes("last7days")) {
      if (qr.postDate < now - 86400000 * 7) {
        return false;
      }
    }

    if (filterDates.includes("last14days")) {
      if (qr.postDate < now - 86400000 * 14) {
        return false;
      }
    }

    if (filterDates.includes("last30days")) {
      if (qr.postDate < now - 86400000 * 30) {
        return false;
      }
    }

    return true;
  }

  filterByAuthor(qr: QuestionRoot) {
    if (this.filterAuthorSelection === "all") {
      return true;
    }
    if (this.filterAuthorSelection === "instructorsOnly") {
      if (qr.authorType != "INSTRUCTOR") {
        return false;
      }
    }
    if (this.filterAuthorSelection === "studentsOnly") {
      if (qr.authorType != "STUDENT") {
        return false;
      }
    }
    if (this.filterAuthorSelection === "authorName") {
      if (!qr.authorName.includes(document.getElementById("authorNameContains")['value'])) {
        return false;
      }
    }
    return true;
  }

  checkPostMatchFilter(qr: QuestionRoot) {
    return !(!this.filterByTag(qr)
      || !this.filterByFolder(qr)
      || !this.filterByDate(qr)
      || !this.filterByAuthor(qr));

  }

  sortBy(selection: string) {
    //most discussion first
    if (selection === "discussion") {
      this.course.questionRootList.sort((qr1: QuestionRoot, qr2: QuestionRoot) => {
        return (qr2.questionRootAnswerList.length + qr2.followupQuestionList.length)
          - (qr1.questionRootAnswerList.length + qr1.followupQuestionList.length)
      });
    }

    //newest first
    if (selection === "new") {
      this.course.questionRootList.sort((qr1: QuestionRoot, qr2: QuestionRoot) => {
        return qr2.postDate - qr1.postDate
      });
    }

    //oldest first
    if (selection === "old") {
      this.course.questionRootList.sort((qr1: QuestionRoot, qr2: QuestionRoot) => {
        return qr1.postDate - qr2.postDate
      });
    }

    //most view first
    if (selection === "view") {
      this.course.questionRootList.sort((qr1: QuestionRoot, qr2: QuestionRoot) => {
        return qr2.viewCount - qr1.viewCount;
      });
    }

    //most vote first
    if (selection === "vote") {
      this.course.questionRootList.sort((qr1: QuestionRoot, qr2: QuestionRoot) => {
        return qr2.likeCount - qr1.likeCount;
      });
    }
  }

  //======================================
  // QUERY
  //======================================

  query() {
    this.clearArr();
    this.stringToArray();
    let result: QuestionRoot[] = [];

    for (let qr of this.course.questionRootList) {
      let matchSearch = this.checkPostMatchSearch(qr);
      let matchFilter = this.checkPostMatchFilter(qr);

      if (this.showSearch && this.showFilter && matchSearch && matchFilter){
        result.push(qr);
      } else if (this.showSearch && !this.showFilter && matchSearch){
        result.push(qr);
      } else if (!this.showSearch && this.showFilter && matchFilter){
        result.push(qr);
      }
    }
    this.course.questionRootList = result;
    this.isQuerying = true;
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

  //======================================
  // HELPER FUNCTION
  //======================================

  getDate(time: number) {
    let daysAgo = Math.floor((new Date().getTime() - time) / 86400000);
    if (daysAgo === 0) {
      if (new Date().getTime() - time < 60 * 60000) {
        return Math.floor((new Date().getTime() - time) / 60000) + " minutes ago"
      } else if (new Date().getTime() - time < 24 * 60 * 60000) {
        return Math.floor((new Date().getTime() - time) / (60 * 60000)) + " hours ago"
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
    s = s.concat(qr.title, qr.content, qr.authorName);

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

  extractCheckboxValue(name: string) {
    let checkbox = document.getElementsByName(name);
    let checkboxValue = [];

    for (var i = 0; i < checkbox.length; i++) {
      if (checkbox[i]['checked'] === true) {
        checkboxValue.push(checkbox[i]['value']);
      }
    }
    return checkboxValue;
  }

  selectAuthor(author: string) {
    this.filterAuthorSelection = author;
  }
}
