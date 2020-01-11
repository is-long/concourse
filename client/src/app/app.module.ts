import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import {HttpClientModule} from "@angular/common/http";
import { NavbarComponent } from './navbar/navbar.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LoginComponent } from './authentication/login/login.component';
import { LogoutComponent } from './authentication/logout/logout.component';
import {FormsModule} from "@angular/forms";
import { CreateCourseComponent } from './course/create-course/create-course.component';
import { RegisterComponent } from './authentication/register/register.component';
import { CourseHomeComponent } from './course/course-home/course-home.component';
import { RegisterConfirmComponent } from './authentication/register/register-confirm/register-confirm.component';
import { JoinCourseComponent } from './course/join-course/join-course.component';
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import { CreatePostComponent } from './course/post/create-post/create-post.component';
import { ProfileComponent } from './profile/profile.component';
import { PostHomeComponent } from './course/post/post-home/post-home.component';
import {EditorModule} from "@tinymce/tinymce-angular";
import { EditorComponent } from './course/post/editor/editor.component';
import { CourseSettingsComponent } from './course/course-settings/course-settings.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    NavbarComponent,
    DashboardComponent,
    LoginComponent,
    LogoutComponent,
    CreateCourseComponent,
    RegisterComponent,
    CourseHomeComponent,
    RegisterConfirmComponent,
    JoinCourseComponent,
    CreatePostComponent,
    ProfileComponent,
    PostHomeComponent,
    EditorComponent,
    CourseSettingsComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    EditorModule,
    FormsModule,
    FontAwesomeModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {

}
