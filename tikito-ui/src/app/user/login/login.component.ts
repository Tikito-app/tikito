import {Component, EventEmitter, Inject, Input, OnInit, Output} from '@angular/core';
import { CommonModule } from '@angular/common'
import {FormControl, FormGroup} from "@angular/forms";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {DOCUMENT} from "@angular/common";
import {TranslateService} from "@ngx-translate/core";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";
import {HttpRequestData} from "../../dto/http-request-data";
import {HttpService} from "../../service/http.service";
import {Util} from "../../util";
import {MatCardModule} from "@angular/material/card";
import {TranslateModule} from "@ngx-translate/core";
import {ReactiveFormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";
import {I18nModule} from "../../i18n/i18n.module";
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

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  imports: [CommonModule, MatCardModule,
    TranslateModule,
    ReactiveFormsModule,
    MatInputModule,
    MatIconModule,
    //HttpClientModule,
    I18nModule,
    MatButtonModule,
    MatListModule,
    MatOptionModule,
    MatSelectModule,
    MatSidenavModule,
    MatToolbarModule, RouterLink]
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
              private translateService: TranslateService,
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
    if(!Util.isJwtValid(jwt)) {
      jwt = null;
      AuthService.deleteAllCookies();
      // localStorage.clear();
    }
    if(Util.hasText(jwt) && this.redirectUrl != '' && this.redirectUrl != null) {
      window.location.href = this.redirectUrl + jwt;
    } else if(Util.hasText(jwt)) {
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

          if(this.redirectUrl != null) {
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
