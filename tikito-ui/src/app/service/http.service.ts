import {Injectable} from '@angular/core';
import {HttpRequestMethod} from "../dto/http-request-method";
import {HttpClient, HttpEventType, HttpHeaders, HttpRequest, HttpResponse} from "@angular/common/http";
import {HttpRequestData} from "../dto/http-request-data";
import {PagedResult} from "../dto/paged-result";
import {Observable} from "rxjs";
import {Util} from "../util";
import {AuthService} from "./auth.service";
import {Router} from "@angular/router";
import {EnvService} from "./env.service";
import {DialogService} from "./dialog.service";
import {TranslateService} from "./translate.service";

@Injectable({
  providedIn: 'root'
})
export class HttpService {

  constructor(private http: HttpClient,
              private envService: EnvService,
              private router: Router,
              private translateService: TranslateService,
              private dialogService: DialogService) {
  }

  httpGetSingle<T>(type: { new(): T }, httpRequestData: HttpRequestData): Observable<T> {
    httpRequestData = httpRequestData.withRequestMethod(HttpRequestMethod.GET);
    return this.httpRequestObject<T>(type, httpRequestData);
  }

  httpGetList<T>(type: { new(): T }, httpRequestData: HttpRequestData): Observable<T[]> {
    httpRequestData = httpRequestData.withRequestMethod(HttpRequestMethod.GET);
    return this.httpRequestList<T>(type, httpRequestData);
  }

  httpPagedGetList<T>(type: { new(): T }, httpRequestData: HttpRequestData): Observable<PagedResult<T>> {
    httpRequestData = httpRequestData.withRequestMethod(HttpRequestMethod.GET);
    return this.httpPagedRequestList<T>(type, httpRequestData);
  }

  httpPostSingle<T>(type: { new(): T }, httpRequestData: HttpRequestData): Observable<T> {
    httpRequestData = httpRequestData.withRequestMethod(HttpRequestMethod.POST);
    return this.httpRequestObject<T>(type, httpRequestData);
  }

  httpPost<T>(httpRequestData: HttpRequestData): Observable<T> {
    httpRequestData = httpRequestData.withRequestMethod(HttpRequestMethod.POST);
    return this.basicHttpRequestWithErrorHandling<T>(httpRequestData);
  }

  httpPostList<T>(type: { new(): T }, httpRequestData: HttpRequestData): Observable<T[]> {
    httpRequestData = httpRequestData.withRequestMethod(HttpRequestMethod.POST);
    return this.httpRequestList<T>(type, httpRequestData);
  }

  httpPagedPostList<T>(type: { new(): T }, httpRequestData: HttpRequestData): Observable<PagedResult<T>> {
    httpRequestData = httpRequestData.withRequestMethod(HttpRequestMethod.POST);
    return this.httpPagedRequestList<T>(type, httpRequestData);
  }

  httpDelete(httpRequestData: HttpRequestData): Observable<void> {
    httpRequestData = httpRequestData.withRequestMethod(HttpRequestMethod.DELETE);
    return this.basicHttpRequestWithErrorHandling(httpRequestData);
  }

  httpPagedRequestList<T>(type: { new(): T }, request: HttpRequestData): Observable<PagedResult<T>> {
    return new Observable<PagedResult<T>>(subscriber => {
      this.basicHttpRequestWithErrorHandling(request).subscribe({
        next: (result) => {
          if (result == null) {
            return;
          }
          let list: any;
          list = (result as PagedResult<T>).items;
          let newResult: T[] = [];
          const len = (list as T[]).length;
          for (let i = 0; i < len; i++) {
            let t = new type() as {};
            let item: T = Object.assign(t, (list as T[])[i]);
            newResult[i] = item;
          }

          (result as PagedResult<T>).items = newResult as T[];
          subscriber.next(result as PagedResult<T>);
        },
        error: (e) => subscriber.error(e),
        complete: () => subscriber.complete()
      });
    });
  }

  httpRequestList<T>(type: { new(): T }, request: HttpRequestData): Observable<T[]> {
    return new Observable<T[]>(subscriber => {
      this.basicHttpRequestWithErrorHandling(request).subscribe({
        next: (result) => {
          if (result == null) {
            return;
          }
          let newResult: T[] = [];
          const len = (result as T[]).length;
          for (let i = 0; i < len; i++) {
            let t = new type() as {};
            let item: T = Object.assign(t, (result as T[])[i]);
            newResult[i] = item;
          }
          subscriber.next(newResult as T[]);
        },
        error: (e) => subscriber.error(e),
        complete: () => subscriber.complete()
      });
    });
  }

  httpRequestObject<T>(type: { new(): T }, request: HttpRequestData): Observable<T> {
    return new Observable<T>(subscriber => {
      this.basicHttpRequestWithErrorHandling(request).subscribe({
        next: (result) => {
          let t = new type() as {};
          result = Object.assign(t, result);
          subscriber.next(result as T);
        },
        error: (e) => subscriber.error(e),
        complete: () => subscriber.complete()
      });
    });
  }

  basicHttpRequestWithErrorHandling<T>(request: HttpRequestData): Observable<T> {
    let _this = this;
    return new Observable(resolve => {
      this.basicHttpRequest<T>(request)
        .subscribe({
          next: (result) => resolve.next(result),
          error: (e) => _this.handleError(e, {}),
          complete: () => resolve.complete()
        });
    });
  }

  basicHttpRequest<T>(request: HttpRequestData): Observable<T> {
    let headers = new HttpHeaders();
    let jwt = AuthService.getCookie('jwt');
    if (request.securityCheck && !Util.isJwtValid(jwt)) {
      AuthService.deleteAllCookies();
      if (!window.location.href.endsWith('/initial-installation')) {
        this.router.navigate(['/login']);
      }
      return new Observable<T>();
    }
    if (Util.hasText(jwt)) {
      headers = headers.set('Authorization', 'Bearer ' + jwt);
    }
    return new Observable<T>(subscriber => {
      if (request.requestMethod == HttpRequestMethod.GET) {
        this.http.get<T>(
          request.fullUrlSupplied ? request.url : this.envService.TIKITO_API_HOSTNAME + request.url,
          {
            headers: headers,
          },
        ).subscribe({
          next: (item) => {
            subscriber.next(item);
          },
          error: (e) => {
            console.log(e);
            subscriber.error(e)
          },
          complete: () => subscriber.complete()
        });
      } else if (request.requestMethod == HttpRequestMethod.DELETE) {
        if (request.body == null) {
          request.body = {};
        }
        const req = new HttpRequest('DELETE',
          request.fullUrlSupplied ? request.url : this.envService.TIKITO_API_HOSTNAME + request.url,
          request.body,
          {
            reportProgress: true,
            responseType: request.responseType,
            headers: headers
          }
        );

        this.http.request<T>(req).subscribe({
          next: (event: any) => subscriber.next(event.body),
          error: (e) => subscriber.error(e),
          complete: () => subscriber.complete()
        });
      } else if (request.requestMethod == HttpRequestMethod.POST || request.requestMethod == HttpRequestMethod.PUT) {
        if (request.body != null) {
          const req = new HttpRequest(request.requestMethod.valueOf(),
            request.fullUrlSupplied ? request.url : this.envService.TIKITO_API_HOSTNAME + request.url,
            request.body,
            {
              reportProgress: true,
              responseType: request.responseType,
              headers: headers
            }
          );

          this.http.request<T>(req).subscribe({
            next: (event: any) => {
              if (event.type === HttpEventType.UploadProgress) {
              } else if (event instanceof HttpResponse) {
                subscriber.next(event.body)
              }
            }, error: (e) => subscriber.error(e)
            , complete: () => subscriber.complete()
          });
        } else {
          this.http.post<T>(
            request.fullUrlSupplied ? request.url : this.envService.TIKITO_API_HOSTNAME + request.url,
            request.params,
            {
              headers: headers,
            }
          ).subscribe(result => subscriber.next(result));
        }
      }
    });
  }

  handleError(error: any, errorMessages: Record<string, string>) {
    if (error.error?.error != null) {
      const key = error.error.error;
      if (errorMessages[key] != null) {
          // todo
        // this.dialogService.snackbar(errorMessages[key], this.translate.transform('close'));
        return;
      }
      this.dialogService.snackbar(this.translateService.translate(key), this.translateService.translate('close'));
    }
  }
}
