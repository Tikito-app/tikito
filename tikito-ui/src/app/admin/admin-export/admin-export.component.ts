import {Component, OnInit, ViewChild} from '@angular/core';
import {MatButton} from "@angular/material/button";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {TranslatePipe} from "@ngx-translate/core";
import {HttpService} from "../../service/http.service";
import {HttpRequestData} from "../../dto/http-request-data";
import {MatIcon} from "@angular/material/icon";
import {Router} from "@angular/router";
import {AdminImportExportSettingComponent} from "../admin-import-export-setting/admin-import-export-setting.component";
import {HttpRequestMethod} from "../../dto/http-request-method";
import {AccountApi} from "../../api/account-api";
import {AuthService} from "../../service/auth.service";
import {Account} from "../../dto/account";
import {MatCheckbox} from "@angular/material/checkbox";

@Component({
  selector: 'app-admin-export',
  standalone: true,
  imports: [
    MatButton,
    ReactiveFormsModule,
    TranslatePipe,
    MatIcon,
    AdminImportExportSettingComponent,
    MatCheckbox
  ],
  templateUrl: './admin-export.component.html',
  styleUrl: './admin-export.component.scss'
})
export class AdminExportComponent implements OnInit {

  form: FormGroup;
  accounts: Account[];

  @ViewChild(AdminImportExportSettingComponent)
  settingsComponent: AdminImportExportSettingComponent;

  constructor(private httpService: HttpService,
              private router: Router,
              private accountApi: AccountApi,
              private authService: AuthService) {
  }

  ngOnInit() {
    this.authService.onSystemReady((_: any) => {
      this.accountApi.getAccounts().subscribe(accounts => {
        let formData: any = {};
        accounts.forEach(account => {
          formData['account' + account.id] = new FormControl('');
        })
        this.form = new FormGroup(formData);
        this.accounts = accounts;
      });
    });
  }

  onExportClicked() {
    this.httpService.basicHttpRequestWithErrorHandling(new HttpRequestData()
      .withRequestMethod(HttpRequestMethod.POST)
      .withUrl('/api/admin/export')
      .withBody({
        settings: this.settingsComponent.getSettings(),
        accountIds: this.getAccountIds()
      }))
      .subscribe((data: any) => {
        const blob = new Blob([JSON.stringify(data)], {type: 'text/plain'});
        const fileURL = URL.createObjectURL(blob);
        const downloadLink = document.createElement('a');

        downloadLink.href = fileURL;
        downloadLink.download = 'export.tikito.json';
        document.body.appendChild(downloadLink);
        downloadLink.click();
      });
  }

  routeToAdmin() {
    this.router.navigate(['/admin']);
  }

  private getAccountIds(): number[] {
    let accountIds: number[] = [];
    this.accounts.forEach(account => {
      if(this.form.value['account' + account.id]) {
        accountIds.push(account.id);
      }
    });
    return accountIds;
  }
}
