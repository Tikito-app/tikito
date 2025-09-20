import {Observable} from "rxjs";
import {HttpService} from "../service/http.service";
import {HttpRequestData} from "../dto/http-request-data";
import {Injectable} from "@angular/core";
import {Overview} from "../dto/overview";

@Injectable({
  providedIn: 'root'
})
export class OverviewApi {

  constructor(private http: HttpService) {
  }

  getOverview(): Observable<Overview> {
    return this.http.httpGetSingle<Overview>(Overview, new HttpRequestData()
      .withUrl('/api/overview'))
  }
}
