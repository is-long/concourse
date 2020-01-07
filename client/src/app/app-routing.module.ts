import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {LoginComponent} from "./authentication/login/login.component";
import {LogoutComponent} from "./authentication/logout/logout.component";
import {NegateLoggedInGuard} from "./guards/negate-logged-in.guard";
import {LoggedInGuard} from "./guards/logged-in.guard";
import {InstructorGuard} from "./guards/instructor.guard";
import {CreateCourseComponent} from "./course/create-course/create-course.component";
import {ConfirmInvitePageComponent} from "./course/confirm-invite-page/confirm-invite-page.component";
import {CourseHomeComponent} from "./course/course-home/course-home.component";
import {InvolvedInCourseGuard} from "./guards/involved-in-course.guard";
import {RegisterComponent} from "./authentication/register/register.component";
import {RegisterConfirmComponent} from "./authentication/register/register-confirm/register-confirm.component";
import {JoinCourseComponent} from "./course/join-course/join-course.component";


const routes: Routes = [
  {path: '', pathMatch: 'full', redirectTo: 'home'},
  {
    path: 'home',
    canActivate: [NegateLoggedInGuard],
    pathMatch: 'full', component: HomeComponent
  },
  {
    path: 'dashboard',
    canActivate: [LoggedInGuard],
    pathMatch: 'full', component: DashboardComponent
  },
  {
    path: 'logout',
    canActivate: [LoggedInGuard],
    pathMatch: 'full', component: LogoutComponent
  },
  {
    path: 'login',
    canActivate: [NegateLoggedInGuard],
    pathMatch: 'full', component: LoginComponent
  },
  {
    path: 'register',
    canActivate: [NegateLoggedInGuard],
    pathMatch: 'full', component: RegisterComponent
  },
  {
    path: 'register/confirm/:confirmationId',
    canActivate: [NegateLoggedInGuard],
    pathMatch: 'full', component: RegisterConfirmComponent
  },
  {
    path: 'course/:courseId/home',
    canActivate: [LoggedInGuard, InvolvedInCourseGuard],
    pathMatch: 'full', component:CourseHomeComponent
  },
  {
    path: 'course/create',
    canActivate: [LoggedInGuard, InstructorGuard],
    pathMatch: 'full', component: CreateCourseComponent
  },

  {
    path: 'course/join',
    canActivate: [LoggedInGuard],
    pathMatch: 'full', component: JoinCourseComponent
  },

  {
    path: '**',
    redirectTo: 'home',
    canActivate: [NegateLoggedInGuard],
  },
  {
    path: '**',
    canActivate: [LoggedInGuard],
    redirectTo: 'dashboard',
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
