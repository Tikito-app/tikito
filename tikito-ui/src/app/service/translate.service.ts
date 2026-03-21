import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class TranslateService {
  translations: Record<string, string> = {};
  selectedLanguage: string = 'en';

  constructor(private http: HttpClient) {
    let translations = localStorage.getItem('translations');
    if(translations != null) {
      this.translations = JSON.parse(translations);
    }

    this.http.get('/assets/i18n/en.json')
      .subscribe(result => {
        this.translations = result as Record<string, string>;
        localStorage.setItem('translations', JSON.stringify(this.translations));
      });
  }

  translate(key: string) {
    return this.translations[key];
  }
}
