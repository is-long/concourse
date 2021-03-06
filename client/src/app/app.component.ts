import {Component} from '@angular/core';
import {AuthService} from "./services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Concourse';

  private authenticated: boolean;

  constructor(private authService: AuthService,
              private router: Router) {
    this.authService.isAuth.subscribe(
      auth => {
        this.changeAuthenticated(auth);
      }
    );
  }

  private changeAuthenticated(authed: boolean) {
    this.authenticated = authed;
  }
}
