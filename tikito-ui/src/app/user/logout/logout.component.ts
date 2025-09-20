import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'
import {AuthService} from "../../service/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-logout',
  standalone: true,
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss'],
  imports: [CommonModule]
})
export class LogoutComponent {
  constructor(private router: Router,
              private authService: AuthService,) {
    AuthService.deleteAllCookies();
    authService.userLoggedOutSubject$.next();
    router.navigate(['']);
  }
}
