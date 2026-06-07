import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Job} from '../dto/job';
import {HttpService} from '../service/http.service';
import {HttpRequestData} from '../dto/http-request-data';

@Injectable({
  providedIn: 'root'
})
export class JobApiService {

  constructor(private httpService: HttpService) {
  }

  getJobs(): Observable<Job[]> {
    return this.httpService.httpGetList<Job>(Job, new HttpRequestData()
      .withUrl('/api/admin/jobs'));
  }
}
