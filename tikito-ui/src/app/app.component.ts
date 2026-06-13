import {Component, OnInit} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {TopHeaderComponent} from "./top-header/top-header.component";
import {FormsModule} from "@angular/forms";
import {UserPreferenceService} from "./service/user-preference-service";
import {AuthService} from "./service/auth.service";
import {UserPreference} from "./dto/user-preference";
import {TranslateService} from "@ngx-translate/core";
import {CacheService} from "./service/cache-service";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    TopHeaderComponent,
    FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',

})
export class AppComponent implements OnInit {
  title = 'tikito-ui';

  constructor(private cacheService: CacheService, // don't remove, this initializes the cache service
              private authService: AuthService,
              private translateService: TranslateService) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady(() => {
      console.log('loaded')
      let language = UserPreferenceService.get<string>(UserPreference.LANGUAGE, 'en');

      this.translateService.addLangs(['en', 'nl', 'de', 'es', 'fr', 'it', 'uk', 'zh']);

      this.translateService.use(language);
    });
  }
}
