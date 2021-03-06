import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Token} from "../../shared/token";
import {Router} from "@angular/router";
import {UserService} from "../../services/user.service";
import {User} from "../../shared/user/user";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  isCodeSent: boolean = false;
  isRegistered: boolean = true;
  email: string;
  code: string;

  constructor(private auth: AuthService, private router: Router, private userService: UserService) {
  }

  ngOnInit() {
  }

  sendCode() {
    let token: Token = new Token();
    token.email = this.email;

    this.auth.sendCode(token).subscribe(
      data => {
        this.isCodeSent = data;
      }
    );
  }

  submitEmail(event) {
    //check if registered
    event.preventDefault();
    let user: User = new User();
    user.email = this.email;

    this.userService.isRegistered(user).subscribe(
      data => {
        if (data) {
          this.sendCode();
        } else {
          this.isRegistered = false;
        }
      }
    );
  }

  signIn() {
    let token: Token = new Token();
    token.code = this.code;
    token.email = this.email;

    this.auth.validateToken(token).subscribe(
      data => {
        if (data) {
          this.auth.setAuthenticated(true, token).subscribe(
            d => {
              this.auth.isAuth.emit(true);
              localStorage.setItem('sessionId', d.sessionId);
              localStorage.setItem('email', d.email);
              this.router.navigateByUrl('/dashboard');
            }
          )
          ;
        } else {
          this.auth.isAuth.emit(false);
          localStorage.removeItem('sessionId');
          localStorage.removeItem('email');
          this.router.navigateByUrl('/login');
        }
      }
    );
  }
}
