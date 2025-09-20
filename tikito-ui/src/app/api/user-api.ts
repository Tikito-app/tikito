import {Observable} from "rxjs";
import {HttpService} from "../service/http.service";
import {HttpRequestData} from "../dto/http-request-data";
import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class UserApi {

  constructor(private http: HttpService) {
  }

  getPreferences(): Observable<{}> {
    return this.http.basicHttpRequest(new HttpRequestData()
      .withUrl('/api/user-preference'))
  }

  setPreference(key: string, value: string): Observable<void> {
    return this.http.httpPost<void>(new HttpRequestData()
      .withUrl('/api/user-preference')
      .withBody({
        key: key,
        value: value
      }))
  }
}
