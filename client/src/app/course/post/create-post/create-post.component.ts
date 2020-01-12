import {Component, Input, OnInit} from '@angular/core';
import {QuestionRoot} from "../../../shared/post/question-root";
import {Router} from "@angular/router";
import {User} from "../../../shared/user/user";
import {UserService} from "../../../services/user.service";
import {CourseService} from "../../../services/course.service";

@Component({
  selector: 'app-create-post',
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.css']
})
export class CreatePostComponent implements OnInit {

  title: string;
  contentHTML: string;
  courseId: string;
  user: User;
  folders: string[];
  selectedFolder: string;

  constructor(private userService: UserService,private router: Router, private courseService: CourseService) {
    let arr = router.url.split("/");
    this.courseId = arr[arr.length - 3].toString();
    this.userService.getSelf().subscribe(
      data => {
        this.user = data;

        this.courseService.getCourseFolders(this.courseId).subscribe(
          folders => {
            this.folders = folders;
          }
        )
      }
    )
  }

  ngOnInit() {
  }

  selectFolder(folderName:string){
    this.selectedFolder = folderName;
  }

  onSubmit(){
    let questionRoot: QuestionRoot = new QuestionRoot();
    questionRoot.courseId = this.courseId;
    questionRoot.postDate = new Date().getTime();
    questionRoot.content = this.contentHTML;
    questionRoot.authorName = this.user.name;
    questionRoot.authorType = this.user.role;
    questionRoot.authorUserId = this.user.email;
    questionRoot.title = this.title;
    questionRoot.folder = this.selectedFolder;

    this.courseService.addQuestionRoot(questionRoot).subscribe(
      data => {
        if (data != null){
          this.router.navigateByUrl('course/' + this.courseId + "/post/" + data.id)
        } else {
          this.router.navigateByUrl('course/' + this.courseId)
        }
      }
    );
  }
}
