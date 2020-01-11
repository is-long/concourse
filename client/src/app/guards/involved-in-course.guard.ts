import { Injectable } from '@angular/core';
import {
  CanActivate,
  CanActivateChild,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
  Router
} from '@angular/router';
import { Observable } from 'rxjs';
import {CourseService} from "../services/course.service";

@Injectable({
  providedIn: 'root'
})
export class InvolvedInCourseGuard implements CanActivate, CanActivateChild {

  constructor(private courseService: CourseService, private router: Router) {
  }

  courseId: string;

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    //temporary fix: find the segment that has length 32 (all course has id of length 32)
    for (let segment of next.url){
      if (segment.toString().length === 32){
        this.courseId = segment.toString();
        break;
      }
    }

    //check if the email from session id is a student or an instructor
    return new Promise((resolve) => {
      this.courseService.checkMember(this.courseId).subscribe(
        data => {
          if (data != null){
            resolve(true);
          } else {
            this.router.navigateByUrl('/dashboard');
            resolve(false);
          }
        }
      )
    });
  }
  canActivateChild(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return true;
  }
}
