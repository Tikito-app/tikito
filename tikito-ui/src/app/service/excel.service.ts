import {Injectable} from "@angular/core";
import {ImportFileProcessState} from "../dto/import-file-process-state";
import {Observable} from "rxjs";
import * as XLSX from "xlsx";
import {CsvService} from "./csv.service";

@Injectable({
  providedIn: 'root',
})
export class ExcelService {
  constructor(private csvService: CsvService) {
  }

  parseExcelFile(state: ImportFileProcessState): Observable<void> {
    return new Observable<void>(subscriber => {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const workbook = XLSX.read(e.target.result, {type: 'binary'});
        const firstSheetName = workbook.SheetNames[0];
        const worksheet = workbook.Sheets[firstSheetName];
        let excelDataCsv = XLSX.utils.sheet_to_csv(worksheet);
        this.csvService.parseCsvString(excelDataCsv, state).subscribe(() => {
          subscriber.next();
        })
      };
      reader.readAsBinaryString(state.file); // todo
    });
  }
}
