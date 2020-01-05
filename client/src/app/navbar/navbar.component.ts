import {Component, Input, OnInit} from '@angular/core';
import {AuthService} from "../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  authenticated: boolean;

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

  ngOnInit() {
  }
}
