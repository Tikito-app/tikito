import {Component, Inject, OnInit, DOCUMENT, ChangeDetectionStrategy} from '@angular/core';

import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {HttpRequestData} from "../../dto/http-request-data";
import {HttpService} from "../../service/http.service";
import {Util} from "../../util";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";
import {MatListModule} from "@angular/material/list";
import {MatOptionModule} from "@angular/material/core";
import {MatSelectModule} from "@angular/material/select";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatToolbarModule} from "@angular/material/toolbar";
import {AuthService} from "../../service/auth.service";
import {LoggedInUser} from "../../dto/logged-in-user";
import {ServerResponse} from "../../dto/server-response";
import {EnvService} from "../../service/env.service";
import {TranslatePipe} from "@ngx-translate/core";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatCardModule, ReactiveFormsModule, MatInputModule, MatIconModule, MatButtonModule, MatListModule, MatOptionModule, MatSelectModule, MatSidenavModule, MatToolbarModule, TranslatePipe]
})
export class LoginComponent implements OnInit {
  form: FormGroup;

  showPasswordFlag: boolean = false;

  redirectUrl: string;

  authenticated: boolean;

  errorMessage: string = '';

  constructor(private http: HttpService,
              private router: Router,
              @Inject(DOCUMENT) private document: Document,
              private route: ActivatedRoute,
              private authService: AuthService,
              private envService: EnvService,
              private httpClient: HttpClient) {
  }

  ngOnInit() {
    this.form = new FormGroup({
      email: new FormControl(''),
      password: new FormControl(''),
    });

    this.redirectUrl = this.route.snapshot.queryParams['redirect'];

    // let jwt = localStorage.getItem('jwt');
    let jwt: string | null = this.authService.getJwtFromStorage();
    if (!Util.isJwtValid(jwt)) {
      jwt = null;
      AuthService.deleteAllCookies();
      // localStorage.clear();
    }
    if (Util.hasText(jwt) && this.redirectUrl != '' && this.redirectUrl != null) {
      window.location.href = this.redirectUrl + jwt;
    } else if (Util.hasText(jwt)) {
      this.authenticated = true;
    }
  }

  submit() {
    if (this.form.valid) {
      this.http.httpPostSingle<LoggedInUser>(
        LoggedInUser,
        new HttpRequestData()
          .withUrl(this.envService.TIKITO_API_HOSTNAME + '/api/user/login')
          .withFullUrlSupplied(true)
          .withSecurityCheck(false)
          .withBody({
            'email': this.form.value.email,
            'password': this.form.value.password
          })).subscribe({
        next: (loggedInUser) => {
          this.authService.storeLoggedInUser(loggedInUser);
          this.authService.setLoggedInUser(loggedInUser);
          this.authService.userLoggedInSubject$.next(loggedInUser);
          // localStorage.setItem('jwt', jwt.jwt);
          // this.authService.setCookie('jwt', jwt.jwt, 7);

          if (this.redirectUrl != null) {
            window.location.href = this.redirectUrl;// + jwt.jwt;
          } else {
            this.router.navigate(['/']);
          }
        }, error: (e) => {
          if (e.error.error == ServerResponse.InvalidCredentialsException) {
            this.errorMessage = 'Invalid email or password.';
            // subscriber.error(e.error);
          }
        }
      });
    }
  }

  logout() {
    // localStorage.clear();
    AuthService.deleteAllCookies();
    this.authenticated = false;
  }

  routeToRegister() {
    this.router.navigate(['/register']);
  }
}
