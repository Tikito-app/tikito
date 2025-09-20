import { NgModule } from '@angular/core';
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {TranslateLoader, TranslateModule, TranslateService} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";

@NgModule({
    imports: [
        HttpClientModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: translateLoaderFactory,
                deps: [HttpClient]
            }
        }),
    ],
    exports: [TranslateModule]
})
//https://indepth.dev/posts/1047/implementing-multi-language-angular-applications-rendered-on-server
export class I18nModule {
    constructor(translate: TranslateService) {
        translate.addLangs(['en', 'nl', 'tr']);
        const browserLang = translate.getBrowserLang() as string;
        translate.use(browserLang.match(/en|nl|tr/) ? browserLang : 'en');
    }
}

export function translateLoaderFactory(httpClient: HttpClient) {
    return new TranslateHttpLoader(httpClient, "./assets/i18n/", ".json");
}
