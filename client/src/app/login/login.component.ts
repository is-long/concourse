import {Component, OnInit} from '@angular/core';
import {AuthService} from "../services/auth.service";
import {Token} from "../shared/token";
import {Router} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  isCodeSent: boolean = false;

  email: string;
  code: string;

  constructor(private auth: AuthService, private router: Router) {
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

  ngOnInit() {
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
              sessionStorage.setItem('sessionId', d.sessionId);
              sessionStorage.setItem('email', d.email);
              this.router.navigateByUrl('/dashboard');
            }
          )
          ;
        } else {
          this.auth.isAuth.emit(false);
          sessionStorage.removeItem('sessionId');
          sessionStorage.removeItem('email');
          this.router.navigateByUrl('/login');
        }
      }
    );
  }

  submitEmail(event) {
    this.sendCode();
    event.preventDefault();
  }
}
