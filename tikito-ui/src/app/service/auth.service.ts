import {Inject, Injectable} from '@angular/core';
import {Observable, Subject} from "rxjs";
import {HttpService} from "./http.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Util} from "../util";
import {HttpRequestData} from "../dto/http-request-data";
import {LoggedInUser} from "../dto/logged-in-user";
import {CacheService} from "./cache-service";
import {HttpRequestMethod} from "../dto/http-request-method";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  loggedInUser: LoggedInUser;

  systemReadySubject$ = new Subject<LoggedInUser | null>();
  userLoggedInSubject$ = new Subject<LoggedInUser>();
  userLoggedOutSubject$ = new Subject<void>();
  systemReady: boolean = false;

  constructor(private http: HttpService,
              private route: ActivatedRoute,
              private router: Router,
              @Inject('environment') private environment: any) {

    this.initialiseUserSession().subscribe(loggedIn => {
      this.systemReady = true;
      CacheService.init().subscribe(() => {
        this.systemReadySubject$.next(this.loggedInUser);
      });
    });
  }

  onSystemReady(func: any) {
    if (this.systemReady) {
      func(this.loggedInUser);
    } else {
      this.systemReadySubject$.subscribe(loggedInUser => {
        func(loggedInUser);
      });
    }
  }

  onUserLoggedIn(func: any) {
    this.userLoggedInSubject$.subscribe(loggedInUser => {
      func(loggedInUser);
    })
  }

  onUserLoggedOut(func: any) {
    this.userLoggedOutSubject$.subscribe(() => {
      func();
    })
  }

  getLoggedInUser(): LoggedInUser {
    return this.loggedInUser;
  }

  isLoggedIn(): boolean {
    return this.loggedInUser != null;
  }

  setLoggedInUser(loggedInUser: LoggedInUser) {
    this.loggedInUser = loggedInUser;
  }

  initialiseUserSession(): Observable<boolean> {
    return new Observable<boolean>(subscriber => {
      let userJson = AuthService.getCookie('logged-in-user');

      this.assertNotInitialInstallation().subscribe(() => {
        if (userJson != null && userJson != '') {
          let loggedInUser = JSON.parse(userJson);

          if (!Util.isJwtValid(loggedInUser.jwt)) {
            // do something?
            subscriber.next(false);
          } else {
            this.loggedInUser = loggedInUser; // store the old object temporarily because the http service needs the jwt.
            this.http.httpGetSingle(LoggedInUser, new HttpRequestData().withUrl('/api/user')).subscribe(
              (updatedLoggedInUser) => {
                this.loggedInUser = updatedLoggedInUser;
                subscriber.next(true);
              });
          }
        } else {
          subscriber.next(false);
        }
      });
    });
  }

  assertNotInitialInstallation(): Observable<void> {
    return new Observable(subscriber => {
      this.http.basicHttpRequest(new HttpRequestData()
        .withSecurityCheck(false)
        .withUrl('/api/user/initial-installation')
        .withRequestMethod(HttpRequestMethod.GET))
        .subscribe(initialInstallation => {
          if (initialInstallation && !window.location.href.endsWith('/initial-installation')) {
            this.systemReady = true;
            this.router.navigate(['/initial-installation']);
          } else {
            subscriber.next();
          }
        });
    });

  }

  jwtToObject(jwt: string) {
    return JSON.parse(atob(jwt.split('.')[1]));
  }


  checkForJwtInUrl(): Observable<void> {
    return new Observable<void>(subscriber => {
      // this.route.queryParams
      //   .subscribe(params => {
      //     if (params['jwt'] != null) {
      //       let jwt = params['jwt'];
      //       localStorage.setItem('jwt', jwt);
      //     }
      subscriber.next();
      // });
    })
  }

  storeLoggedInUser(loggedInUser: LoggedInUser) {
    this.setCookie('jwt', loggedInUser.jwt, 7);
    this.setCookie('logged-in-user', JSON.stringify(loggedInUser), 7);
//     this.initialiseUserSession();
  }

  getJwtFromStorage() {
    return AuthService.getCookie('jwt');
  }

  static getCookie(cname: string) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) == ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) == 0) {
        return c.substring(name.length, c.length);
      }
    }
    return "";
  }

  isAuthenticated(): boolean {
    let jwt = this.getJwtFromStorage();
    if (Util.hasText(jwt) && Util.isJwtValid(jwt)) {
      return true;
    }
    AuthService.deleteAllCookies();
    localStorage.clear();
    return false;
  }


  setCookie(cname: string, cvalue: string, exdays: number) {
    const d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    let expires = "expires=" + d.toUTCString();
    let domain = ';Domain=.' + window.location.hostname;

    document.cookie = cname + "=" + cvalue + ";" + expires + ";SameSite=Strict;path=/" + domain;
  }

  static deleteAllCookies() {
    localStorage.clear();
    let cookies = document.cookie.split(";");
    for (let i = 0; i < cookies.length; i++) {
      let cookie = cookies[i];
      let eqPos = cookie.indexOf("=");
      let name = eqPos > -1 ? cookie.substring(0, eqPos) : cookie;
      document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;SameSite=Strict";
    }
  }
}
