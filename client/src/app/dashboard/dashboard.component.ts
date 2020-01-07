import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthService} from "../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {

  email;
  isInstructor: boolean = true;

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit() {
    this.email = sessionStorage.getItem('email');
  }

  ngOnDestroy(): void {
  }


}
