import {AfterViewInit, Component, OnInit} from '@angular/core';
import {Post} from "../../../shared/post/post";
import {CourseService} from "../../../services/course.service";
import {Router} from "@angular/router";
import {User} from "../../../shared/user/user";
import {UserService} from "../../../services/user.service";
import {Course} from "../../../shared/course";

@Component({
  selector: 'app-edit-post',
  templateUrl: './edit-post.component.html',
  styleUrls: ['./edit-post.component.css']
})
export class EditPostComponent implements OnInit {

  contentHTML: string;
  courseId: string;
  postId: string;

  constructor(private userService: UserService, private courseService: CourseService, private router: Router) {
    let arr = router.url.split("/");
    this.courseId = arr[arr.length - 4].toString();
    this.postId = arr[arr.length - 2].toString();

    this.courseService.getPost(this.courseId, this.postId).subscribe(
      data => {
        this.postId = data.id;
        this.contentHTML = data.content;
      }
    );
  }

  ngOnInit() {
  }

  onSubmit() {
    let post: Post = new Post();
    post.id = this.postId;
    post.content = this.contentHTML;
    this.courseService.editPost(this.courseId, this.postId, post).subscribe(
      success => {
        this.router.navigateByUrl('/course/' + this.courseId + '/post/' + this.postId);
      }
    );
  }
}
