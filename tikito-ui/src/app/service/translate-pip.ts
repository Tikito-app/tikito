import {PipeTransform} from "@angular/core";
import {TranslateService} from "./translate.service";

export class ReversePipe implements PipeTransform {
  constructor(private translateService: TranslateService) {
  }

  transform(value: string): string {
    return this.translateService.translate(value);
  }
}
