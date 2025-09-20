import {ApplicationConfig} from '@angular/core';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {provideHttpClient} from "@angular/common/http";
import { environment } from '../environments/environment';
import {provideTranslateService} from "@ngx-translate/core";

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(),
    {
      provide: 'environment', // you can also use InjectionToken
      useValue: environment
    },
    provideTranslateService()
  ]
};
