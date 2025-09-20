import {Injectable} from "@angular/core";
import {Papa, ParseResult} from "ngx-papaparse";
import {ImportFileProcessState} from "../dto/import-file-process-state";
import {Observable, Observer} from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class CsvService {

  constructor(private papa: Papa) {

  }

  parseCsvFile(state: ImportFileProcessState): Observable<void> {
    return new Observable<any>((observer: Observer<void>) => {
      this.papa.parse(state.file, {
        delimiter: state.csvSeparator,
        header: true,
        complete: result => {
          this.processContent(state, result);
          observer.next();
        }
      });
    });
  }

  parseCsvString(data: string, state: ImportFileProcessState): Observable<void> {
    return new Observable<any>((observer: Observer<void>) => {
      this.papa.parse(data, {
        delimiter: state.csvSeparator,
        header: true,
        complete: result => {
          this.processContent(state, result);
          observer.next();
        }
      });
    });
  }

  processContent(state: ImportFileProcessState, result: ParseResult) {
    state.parsedContent = [];
    let line = [];
    for (let header of result.meta.fields) {
      line.push(this.normalizeCsvHeader(header));
    }
    state.parsedContent.push(line);

    for (let csvLine of result.data) {
      let line = [];
      for (let header of result.meta.fields) {
        line.push(csvLine[header]);
      }
      state.parsedContent.push(line);
    }
  }

  normalizeCsvHeader(header: string): string {
    if (header.startsWith('_')) {
      return '';
    }
    return header;
  }
}
