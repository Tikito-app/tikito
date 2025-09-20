import {HttpParams} from "@angular/common/http";
import {HttpRequestMethod} from "./http-request-method";

export class HttpRequestData {
  static RequestNotAllowedException = 'RequestNotAllowedException';
  static ResourceNotFoundException = 'ResourceNotFoundException';

  hostname: string|null;
  url: string;
  responseType: 'arraybuffer' | 'blob' | 'json' | 'text' = 'json';
  requestMethod: HttpRequestMethod = HttpRequestMethod.GET;
  params: HttpParams;
  body: {};
  cacheable: boolean = false; // todo: idea? https://blog.logrocket.com/caching-with-httpinterceptor-in-angular/
  fullUrlSupplied: boolean = false;
  securityCheck: boolean = true;
  pagedRequest: boolean = false;

  withHostname(hostname: string): HttpRequestData {
    this.hostname = hostname;
    return this;
  }

  withUrl(url: string): HttpRequestData {
    this.url = url;
    return this;
  }

  withFullUrl(url: string): HttpRequestData {
    this.url = url;
    this.fullUrlSupplied = true;
    return this;
  }

  withResponseType(responseType: 'arraybuffer' | 'blob' | 'json' | 'text'): HttpRequestData {
    this.responseType = responseType;
    return this;
  }

  withRequestMethod(requestMethod: HttpRequestMethod): HttpRequestData {
    this.requestMethod = requestMethod;
    return this;
  }

  withParams(params: HttpParams): HttpRequestData {
    this.params = params;
    return this;
  }

  withBody(body: { }): HttpRequestData {
    this.body = body;
    return this;
  }

  withFullUrlSupplied(fullUrlSupplied: boolean) {
    this.fullUrlSupplied = fullUrlSupplied;
    return this;
  }

  withSecurityCheck(securityCheck: boolean) {
    this.securityCheck = securityCheck;
    return this;
  }

  withPagedRequest(pagedRequest: boolean) {
    this.pagedRequest = pagedRequest;
    return this;
  }
}
