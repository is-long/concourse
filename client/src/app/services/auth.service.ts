import {EventEmitter, Injectable, Output} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Token} from "../shared/token";
import {environment} from "../../environments/environment.prod";
import {Session} from "../shared/session";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private url: string = environment.apiUrl;

  @Output() isAuth: EventEmitter<any> = new EventEmitter();

  constructor(private http: HttpClient) {
    this.url = this.url.replace("https", "http");
  }

  sendCode(token: Token) {
    return this.http.post<boolean>(this.url + "/token/new", token);
  }

  validateToken(token: Token) {
    return this.http.post(this.url + "/token/validate", token);
  }

  setAuthenticated(auth: boolean, token: Token) {
    let session: Session = new Session();
    session.email = token.email;
    return this.http.post<Session>(this.url + '/session/new', session);
  }

  getSession() {
    let session: Session = new Session();
    session.email = localStorage.getItem('email');
    session.sessionId = localStorage.getItem('sessionId');
    return session;
  }

  isAuthenticated() {
    return this.http.post<boolean>(this.url + '/session/validate', this.getSession());
  }

  logout() {
    return this.http.post<boolean>(this.url + '/session/purge', this.getSession());
  }
}
