import {Component, ViewChild} from '@angular/core';
import {MatButton} from "@angular/material/button";
import {TranslatePipe} from "@ngx-translate/core";
import {NgIf} from "@angular/common";
import {SecurityTransactionImportLine} from "../../dto/security/security-transaction-import-line";
import {HttpRequestData} from "../../dto/http-request-data";
import {HttpService} from "../../service/http.service";
import {DialogService} from "../../service/dialog.service";
import {MatIcon} from "@angular/material/icon";
import {Router} from "@angular/router";
import {AdminImportExportSettingComponent} from "../admin-import-export-setting/admin-import-export-setting.component";

@Component({
  selector: 'app-admin-import',
  standalone: true,
  imports: [
    MatButton,
    TranslatePipe,
    NgIf,
    MatIcon,
    AdminImportExportSettingComponent
  ],
  templateUrl: './admin-import.component.html',
  styleUrl: './admin-import.component.scss'
})
export class AdminImportComponent {
  file: File;

  @ViewChild(AdminImportExportSettingComponent)
  settingsComponent: AdminImportExportSettingComponent;

  constructor(private http: HttpService,
              private dialogService: DialogService,
              private router: Router) {
  }

  onFileSelected(event: any) {
    this.file = event.target.files[0];
  }

  onImportClicked() {
    const formData = new FormData();
    formData.append("file", this.file);

    const reader = new FileReader();
    reader.onload = (e: any) => {
      return this.http.httpPost<SecurityTransactionImportLine[]>(new HttpRequestData()
        .withUrl('/api/admin/import')
        .withBody({
          data: e.target.result,
          settings: this.settingsComponent.getSettings()
        }))
        .subscribe(() => this.dialogService.snackbar('Done'));
    };

    reader.readAsText(this.file);
  }

  routeToAdmin() {
    this.router.navigate(['/admin']);
  }
}
