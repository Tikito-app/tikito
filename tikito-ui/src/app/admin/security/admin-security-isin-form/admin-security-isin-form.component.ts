import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {AdminApi} from "../../../api/admin-api";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../../service/auth.service";
import {Util} from "../../../util";
import {MatButton} from "@angular/material/button";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {NgIf} from "@angular/common";
import {TranslatePipe} from "@ngx-translate/core";

@Component({
  selector: 'app-admin-security-isin-form',
  standalone: true,
  imports: [
    MatButton,
    MatCard,
    MatCardContent,
    MatCardHeader,
    MatCardTitle,
    MatFormField,
    MatInput,
    MatLabel,
    NgIf,
    ReactiveFormsModule,
    TranslatePipe
  ],
  providers: [TranslatePipe],
  templateUrl: './admin-security-isin-form.component.html',
  styleUrl: './admin-security-isin-form.component.scss'
})
export class AdminSecurityIsinFormComponent implements OnInit {
  form: FormGroup;
  securityId: number;
  isin: string;


  constructor(private api: AdminApi,
              private router: Router,
              private authService: AuthService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.securityId = Util.getIdFromRoute(this.route, 'securityId') as number;
      this.isin = Util.getFromRoute(this.route, 'isin') as string;
      this.reset();
    });
  }

  reset() {
    let group: any = {
      symbol: new FormControl(''),
      validFrom: new FormControl(''),
      validTo: new FormControl('')
    };
    this.form = new FormGroup(group);

    this.api.getIsin(this.isin).subscribe(isin => {
      this.form.controls['symbol'].setValue(isin.symbol);
      this.form.controls['validFrom'].setValue(isin.validFrom);
      this.form.controls['validTo'].setValue(isin.validTo);
    });
  }

  onSaveButtonClicked() {
    this.api.updateIsin(
      this.isin,
      this.form.value.symbol,
      this.form.value.validFrom,
      this.form.value.validTo).subscribe(isin => {
      this.router.navigate(['/admin/security/' + this.securityId + '/isin']);
    });
  }

  onCancelButtonClicked() {
    this.router.navigate(['/admin/security/' + this.securityId + '/isin']);
  }

  onDeleteButtonClicked() {
    if (this.isin != null) {
      this.api.deleteIsin(this.isin).subscribe(() => this.onCancelButtonClicked());
    } else {
      this.onCancelButtonClicked();
    }
  }

}
