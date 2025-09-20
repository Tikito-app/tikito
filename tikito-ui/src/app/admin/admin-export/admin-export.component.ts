import {Component} from '@angular/core';
import {MatButton} from "@angular/material/button";
import {ReactiveFormsModule} from "@angular/forms";
import {TranslatePipe} from "@ngx-translate/core";
import {HttpService} from "../../service/http.service";
import {HttpRequestData} from "../../dto/http-request-data";
import {MatIcon} from "@angular/material/icon";
import {Router} from "@angular/router";

@Component({
  selector: 'app-admin-export',
  standalone: true,
  imports: [
    MatButton,
    ReactiveFormsModule,
    TranslatePipe,
    MatIcon
  ],
  templateUrl: './admin-export.component.html',
  styleUrl: './admin-export.component.scss'
})
export class AdminExportComponent {

  constructor(private httpService: HttpService,
              private router: Router) {
  }

  onExportClicked() {
    this.httpService.basicHttpRequestWithErrorHandling(new HttpRequestData()
      .withUrl('/api/admin/export')).subscribe((data: any) => {
      const blob = new Blob([JSON.stringify(data)], { type: 'text/plain' });
      const fileURL = URL.createObjectURL(blob);

      const downloadLink = document.createElement('a');
      downloadLink.href = fileURL;
      downloadLink.download = 'export.tikito';
      document.body.appendChild(downloadLink);
      downloadLink.click();
    });
  }

  routeToAdmin() {
    this.router.navigate(['/admin']);
  }
}
