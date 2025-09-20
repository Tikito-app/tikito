import {Injectable} from "@angular/core";
import {NgxCsvParser} from "ngx-csv-parser";
import {Observable} from "rxjs";
import {AccountApi} from "../api/account-api";
import {ImportTypeData} from "../dto/import-type-data";
import {AccountType} from "../dto/account-type";
import {ImportFileProcessState} from "../dto/import-file-process-state";
import {FileType} from "../dto/file-type";

@Injectable({
  providedIn: 'root',
})
export class FileImportService {
  header: boolean = false;
  supportedHeaders: any;

  constructor(private ngxCsvParser: NgxCsvParser,
              private accountApi: AccountApi) {
    this.accountApi.getImporterTypesHeaders().subscribe(supportedHeaders => {
      this.supportedHeaders = supportedHeaders
    });
  }

  setFileTypeAndContent(state: ImportFileProcessState): Observable<void> {
    return new Observable(subscriber => {
      state.fileType = this.determineFileTypeByExtension(state) as FileType;

      if (state.fileType != FileType.EXCEL) {
        this.setFileContent(state).subscribe(() => {
          if (state.fileType == null) {
            this.setFileTypeBasedOnContent(state);
          }
          subscriber.next();
        });
      } else {
        subscriber.next();
      }
    });
  }

  setFileTypeBasedOnContent(state: ImportFileProcessState) {
    if (typeof (state.content) == 'string') {
      let lines = state.content.split("\n");
      if (lines[1] == '940' || lines[1] == 'MT940') {
        state.fileType = FileType.MT940;
      }
    }
  }

  setFileContent(state: ImportFileProcessState): Observable<void> {
    return new Observable<void>(subscriber => {
      let fileReader: FileReader = new FileReader();
      fileReader.onloadend = function (x) {
        state.content = fileReader.result;
        subscriber.next();
      };
      fileReader.readAsText(state.file);
    })
  }

  determineFileTypeByExtension(state: ImportFileProcessState): FileType | null {
    let filename = state.file.name.toLowerCase();
    if (filename.endsWith('sta')) {
      return FileType.MT940;
    } else if (filename.endsWith('xls') || filename.endsWith('xlsx')) {
      return FileType.EXCEL;
    } else if (filename.endsWith('csv')) {
      return FileType.CSV;
    }

    return null;
  }

  determineAccountTypeOnHeaders(state: ImportFileProcessState): void {
    let headerLine = state.parsedContent[0];
    let types: string[] = Object.keys(this.supportedHeaders);

    for (let type of types) {
      let header = this.supportedHeaders[type] as ImportTypeData;

      if (headerLine.length == header.headers.length) {
        let misMatch = false;

        for (let i = 0; i < headerLine.length; i++) {
          if (!this.headerLineEquals(headerLine[i], header.headers[i])) {
            misMatch = true;
          }
        }

        if (!misMatch) {
          state.accountType = this.supportedHeaders[type].accountType as AccountType;
          return;
        }
      }
    }
  }

  headerLineEquals(fileHeader: string, sourceHeader: string): boolean {
    if (!this.hasText(fileHeader) && !this.hasText(sourceHeader)) {
      return true;
    }
    return fileHeader == sourceHeader;
  }

  hasText(str: string) {
    return str != null && str.length > 0;
  }
}
