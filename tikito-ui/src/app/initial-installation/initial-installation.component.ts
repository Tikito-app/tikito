import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AccountApi} from "../api/account-api";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../service/auth.service";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatError, MatFormField, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {TranslatePipe} from "@ngx-translate/core";
import {MatButton} from "@angular/material/button";
import {NgIf} from "@angular/common";
import {UserAccount} from "../dto/user-account";
import {HttpRequestData} from "../dto/http-request-data";
import {HttpService} from "../service/http.service";
import {HttpRequestMethod} from "../dto/http-request-method";
import {CacheService} from "../service/cache-service";

@Component({
  selector: 'app-initial-installation',
  standalone: true,
  imports: [
    FormsModule,
    MatCardContent,
    MatFormField,
    MatIcon,
    MatInput,
    MatLabel,
    ReactiveFormsModule,
    TranslatePipe,
    MatButton,
    MatCard,
    MatCardHeader,
    MatCardTitle,
    MatError,
    MatSuffix,
    NgIf
  ],
  providers: [TranslatePipe],
  templateUrl: './initial-installation.component.html',
  styleUrl: './initial-installation.component.scss'
})
export class InitialInstallationComponent implements OnInit {

  showPasswordFlag = false;
  form: FormGroup;
  errorMessage: string;
  registered: boolean;

  constructor(private api: AccountApi,
              private router: Router,
              private translate: TranslatePipe,
              private http: HttpService,
              private authService: AuthService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {

    this.form = new FormGroup({
      email: new FormControl(''),
      password: new FormControl(''),
      passwordAgain: new FormControl('')
    });

    this.authService.onSystemReady((loggedInUser: any) => {
      this.http.basicHttpRequest(new HttpRequestData()
        .withSecurityCheck(false)
        .withUrl('/api/user/initial-installation')
        .withRequestMethod(HttpRequestMethod.GET))
        .subscribe(initialInstallation => {
          if(!initialInstallation) {
            this.router.navigate(['/']);
          }
        });
    });
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
      }))
      .subscribe({
        next: (userAccount) => {
          this.registered = true;
          setTimeout(() => {
            AuthService.deleteAllCookies();
            CacheService.init().subscribe();
            this.router.navigate(['/login']);
          }, 3000)
        }, error: (error) => {
          if (error.error.error == 'PasswordNotStrongEnoughException') {
            this.errorMessage = 'register/password-not-strong-enough';
          }
        }
      });
  }
}
