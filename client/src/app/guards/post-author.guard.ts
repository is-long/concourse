import { Injectable } from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router} from '@angular/router';
import { Observable } from 'rxjs';
import {CourseService} from "../services/course.service";

@Injectable({
  providedIn: 'root'
})
export class PostAuthorGuard implements CanActivate {


  constructor(private courseService: CourseService, private router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    let courseId: string = next.url[next.url.length - 4].toString();
    let postId: string = next.url[next.url.length - 2].toString();

    return new Promise( (resolve) => {
       this.courseService.isOwnPost(courseId, postId).subscribe(
        isOwnPost => {
          if (isOwnPost){
            resolve(true);
          } else {
            this.router.navigateByUrl('/course/' + courseId);
            resolve(false);
          }
        }
      )
    });



    return true;
  }

}
