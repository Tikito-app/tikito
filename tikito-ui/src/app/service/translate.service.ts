import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class TranslateService {
  translations: Record<string, string>;

  constructor(private http: HttpClient) {

    this.http.get('/assets/i18n/en.json')
      .subscribe(result => {
        this.translations = result as Record<string, string>;
      });
  }

  translate(key: string) {
    return this.translations[key];
  }

}
