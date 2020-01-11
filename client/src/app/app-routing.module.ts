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
import {CourseHomeComponent} from "./course/course-home/course-home.component";
import {InvolvedInCourseGuard} from "./guards/involved-in-course.guard";
import {RegisterComponent} from "./authentication/register/register.component";
import {RegisterConfirmComponent} from "./authentication/register/register-confirm/register-confirm.component";
import {JoinCourseComponent} from "./course/join-course/join-course.component";
import {ProfileComponent} from "./profile/profile.component";
import {PostHomeComponent} from "./course/post/post-home/post-home.component";
import {CreatePostComponent} from "./course/post/create-post/create-post.component";
import {CourseService} from "./services/course.service";
import {CourseSettingsComponent} from "./course/course-settings/course-settings.component";


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
    path: 'profile',
    canActivate: [LoggedInGuard],
    pathMatch: 'full', component: ProfileComponent
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
    path: 'course/:courseId/settings',
    canActivate: [LoggedInGuard, InstructorGuard],
    pathMatch: 'full', component: CourseSettingsComponent
  },
  {
    path: 'course/:courseId/post/new',
    canActivate: [LoggedInGuard, InvolvedInCourseGuard],
    pathMatch: 'full', component: CreatePostComponent
  },
  {
    path: 'course/:courseId/post/:postId',
    canActivate: [LoggedInGuard, InvolvedInCourseGuard],
    pathMatch: 'full', component: PostHomeComponent
  },
  {
    path: 'course/:courseId',
    canActivate: [LoggedInGuard, InvolvedInCourseGuard],
    pathMatch: 'full', component:CourseHomeComponent
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
