import {Injectable} from "@angular/core";
import {HttpService} from "./http.service";
import {HttpRequestData} from "../dto/http-request-data";
import {TikitoVersion} from "../dto/tikito-version";
import {v4 as uuidv4} from "uuid";
import {Observable} from "rxjs";
import {Util} from "../util";

@Injectable({
  providedIn: 'root'
})
export class VersionCheckService {
  constructor(private httpService: HttpService) {
  }

  checkVersion(): Observable<boolean> {
    return new Observable(observer => {
      this.httpService.httpGetSingle(TikitoVersion, new HttpRequestData()
        .withSecurityCheck(false)
        .withFullUrlSupplied(true)
        .withUrl('/assets/tikito-version.json?r=' + uuidv4())).subscribe(tikitoVersion => {
        let storedVersion = localStorage.getItem('tikito-version');

        if (Util.hasText(storedVersion) && tikitoVersion.version != storedVersion) {
          observer.next(true);
        } else {
          observer.next(false);
        }
      });
    });
  }
}