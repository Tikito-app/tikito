import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Log} from '../dto/log';
import {HttpService} from '../service/http.service';
import {HttpRequestData} from '../dto/http-request-data';

@Injectable({
  providedIn: 'root'
})
export class LogApiService {

  constructor(private httpService: HttpService) {
  }

  getLogs(): Observable<Log[]> {
    return this.httpService.httpGetList<Log>(Log, new HttpRequestData()
      .withUrl('/api/admin/logs'));
  }

  deleteLog(id: number): Observable<void> {
    return this.httpService.httpDelete(new HttpRequestData()
      .withUrl('/api/admin/log/' + id));
  }
}
