import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Route, Router} from "@angular/router";
import {UserService} from "../../../services/user.service";

@Component({
  selector: 'app-register-confirm',
  templateUrl: './register-confirm.component.html',
  styleUrls: ['./register-confirm.component.css']
})
export class RegisterConfirmComponent implements OnInit {
  isInvalidCode: boolean = false;

  constructor(private router: Router, private userService: UserService, private route: ActivatedRoute) {
    let arr = this.router.url.split('/');
    let confirmationId: string = arr[arr.length - 1];
    this.userService.confirmRegistration(confirmationId).subscribe(
      success => {
        if (success) {
          this.router.navigateByUrl('/login');
        }
      }
    );
  }

  ngOnInit() {

  }

}
