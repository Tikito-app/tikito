import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {TopHeaderComponent} from "./top-header/top-header.component";
import {FormsModule} from "@angular/forms";
import {CacheService} from "./service/cache-service";
import {I18nModule} from "./i18n/i18n.module";
import {UserPreferenceService} from "./service/user-preference-service";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    TopHeaderComponent,
    FormsModule,
    I18nModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',

})
export class AppComponent {
  title = 'tikito-ui';

  constructor(private cacheService: CacheService,
              private userPreferenceService: UserPreferenceService) {
  }
}
