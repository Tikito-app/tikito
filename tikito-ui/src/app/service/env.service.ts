import {Inject, Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvService {
  public TIKITO_API_HOSTNAME = (window as any).__env?.TIKITO_API_HOSTNAME;
  constructor(
    @Inject('environment') private environment: any) {
    if(this.TIKITO_API_HOSTNAME == "${TIKITO_API_HOSTNAME}" || this.TIKITO_API_HOSTNAME == null) {
      this.TIKITO_API_HOSTNAME = this.environment.hostname
    }
  }
}
