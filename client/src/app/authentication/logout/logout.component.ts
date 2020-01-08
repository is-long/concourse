import { Component, OnInit } from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.css']
})
export class LogoutComponent implements OnInit {

  constructor(private auth: AuthService, private router: Router) {
    this.auth.logout().subscribe(
      data => {
        if (data){
          this.auth.isAuth.emit(false);
          localStorage.removeItem('sessionId');
          localStorage.removeItem('email');
          this.router.navigateByUrl('/home');
        }
      }
    );
  }

  ngOnInit() {
  }

}
