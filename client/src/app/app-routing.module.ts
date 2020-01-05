import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {LoginComponent} from "./login/login.component";
import {LogoutComponent} from "./logout/logout.component";
import {NegateLoggedInGuard} from "./auth/negate-logged-in.guard";
import {LoggedInGuard} from "./auth/logged-in.guard";


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
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
