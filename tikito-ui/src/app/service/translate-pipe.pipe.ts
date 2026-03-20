import { Pipe, PipeTransform } from '@angular/core';
import {TranslateService} from "./translate.service";

@Pipe({
  name: 'translatePipe',
  standalone: true
})
export class TranslatePipePipe implements PipeTransform {
  constructor(private translateService: TranslateService) {
  }

  transform(key: string): string {
    let translation = this.translateService.translate(key);
    if(translation == null) {
      return key;
    }
    return translation;
  }

}
