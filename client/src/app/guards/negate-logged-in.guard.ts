import {Injectable} from '@angular/core';
import {
  CanActivate,
  CanActivateChild,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
  Router
} from '@angular/router';
import {Observable} from 'rxjs';
import {LoggedInGuard} from "./logged-in.guard";
import {AuthService} from "../services/auth.service";

@Injectable({
  providedIn: 'root'
})
export class NegateLoggedInGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router, private loggedInGuard: LoggedInGuard) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    if (!localStorage.getItem('email') || !localStorage.getItem('sessionId')){
      return true;
    } else {
      return new Promise((resolve) => {
        this.authService.isAuthenticated().toPromise().then((data) => {
          if (data) {
            this.authService.isAuth.emit(true);
            this.router.navigateByUrl('/dashboard');
            resolve(false);
          } else {
            this.authService.isAuth.emit(false);
            resolve(true);
          }
        });
      })
    }
  };
}
