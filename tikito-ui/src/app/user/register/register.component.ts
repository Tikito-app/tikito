import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {CommonModule} from '@angular/common'
import {HttpService} from "../../service/http.service";
import {ActivatedRoute, Router} from "@angular/router";
import {UserAccount} from "../../dto/user-account";
import {HttpRequestData} from "../../dto/http-request-data";

import {MatCardModule} from "@angular/material/card";
import {TranslateModule} from "@ngx-translate/core";
import {MatInputModule} from "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";
import {I18nModule} from "../../i18n/i18n.module";
import {MatButtonModule} from "@angular/material/button";
import {MatListModule} from "@angular/material/list";
import {MatOptionModule} from "@angular/material/core";
import {MatSelectModule} from "@angular/material/select";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatToolbarModule} from "@angular/material/toolbar";
import {HttpRequestMethod} from "../../dto/http-request-method";

@Component({
  selector: 'app-register',
  standalone: true,
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
  imports: [CommonModule, MatCardModule,
    TranslateModule,
    ReactiveFormsModule,
    MatInputModule,
    MatIconModule,
    I18nModule,
    MatButtonModule,
    MatListModule,
    MatOptionModule,
    MatSelectModule,
    MatSidenavModule,
    MatToolbarModule]
})
export class RegisterComponent implements OnInit {
  showPasswordFlag = false;
  form: FormGroup;
  errorMessage: string;
  registered: boolean;
  firstEverUser: boolean;

  constructor(private http: HttpService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.form = new FormGroup({
      email: new FormControl(''),
      password: new FormControl(''),
    });

    this.http.basicHttpRequestWithErrorHandling(new HttpRequestData()
      .withUrl('/api/user/register')
      .withSecurityCheck(false)
      .withResponseType('text')
      .withRequestMethod(HttpRequestMethod.GET)).subscribe(firstEverUser => {
        this.firstEverUser = firstEverUser == true;
    })
  }

  routeToLogin() {
    this.router.navigate(['/login'])
  }

  onSubmit() {
    if(this.form.value.password != this.form.value.passwordAgain) {
      this.errorMessage = 'register/passwords-do-not-match';
      return;
    }
    this.http.httpPostSingle<UserAccount>(UserAccount, new HttpRequestData()
      .withUrl('/api/user/register')
      .withSecurityCheck(false)
      .withBody({
        email: this.form.value.email,
        password: this.form.value.password,
        passwordAgain: this.form.value.password,
      }))
      .subscribe({
        next: (userAccount) => {
          this.registered = true;
//                 if(this.redirectUrl != null) {
//                   window.location.href = this.redirectUrl;// + jwt.jwt;
//                 } else {
//                   this.router.navigate(['/']);
//                 }
        }, error: (error) => {
          if (error.error.error == 'EmailAlreadyExistsException') {
            this.errorMessage = 'register/email-already-exists';
          } else if (error.error.error == 'PasswordNotStrongEnoughException') {
            this.errorMessage = 'register/password-not-strong-enough';
          }
        }
      });
  }
}
