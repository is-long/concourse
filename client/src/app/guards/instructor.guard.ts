import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router} from '@angular/router';
import {Observable} from 'rxjs';
import {CourseService} from "../services/course.service";
import {UserService} from "../services/user.service";
import {User} from "../shared/user/user";

@Injectable({
  providedIn: 'root'
})
export class InstructorGuard implements CanActivate {


  constructor(private courseService: CourseService, private router: Router,
              private userService: UserService) {
  }

  courseId: string;

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    //used in course/{courseId}/settings
    //temporary fix: find the segment that has length 32 (all course has id of length 32)
    for (let segment of next.url) {
      if (segment.toString().length === 32) {
        this.courseId = segment.toString();
        break;
      }
    }

    return new Promise((resolve) => {
      let user: User;
      this.userService.getSelf().subscribe(
        data => {
          if (data.role === 'INSTRUCTOR') {
            resolve(true);
          } else {
            this.router.navigateByUrl('/course/' + this.courseId);
            resolve(false);
          }
        }
      );
    })
  }
}
