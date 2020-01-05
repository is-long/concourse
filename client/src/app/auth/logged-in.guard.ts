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
import {AuthService} from "../services/auth.service";

@Injectable({
  providedIn: 'root'
})
export class LoggedInGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    if (!sessionStorage.getItem('email') || !sessionStorage.getItem('sessionId')){
      this.router.navigateByUrl('/login');
      return false;
    } else {
      return new Promise((resolve) => {
        this.authService.isAuthenticated().toPromise().then((data) => {
          if (!data) {
            this.authService.isAuth.emit(false);
            this.router.navigateByUrl('/login');
            resolve(false);
          } else {
            this.authService.isAuth.emit(true);
            resolve(true);
          }
        });
      })
    }
  };
}

