import {Component, OnInit} from '@angular/core';
import {MatButton} from "@angular/material/button";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {MatOption} from "@angular/material/core";
import {MatSelect} from "@angular/material/select";
import {NgIf} from "@angular/common";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {TranslatePipe} from "@ngx-translate/core";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../../service/auth.service";
import {Util} from "../../../util";
import {AdminApi} from "../../../api/admin-api";
import {CacheService} from "../../../service/cache-service";

@Component({
  selector: 'app-admin-security-form',
  standalone: true,
  imports: [
    MatButton,
    MatCard,
    MatCardContent,
    MatCardHeader,
    MatCardTitle,
    MatFormField,
    MatIcon,
    MatInput,
    MatLabel,
    MatOption,
    MatSelect,
    NgIf,
    ReactiveFormsModule,
    TranslatePipe
  ],
  templateUrl: './admin-security-form.component.html',
  styleUrl: './admin-security-form.component.scss'
})
export class AdminSecurityFormComponent implements OnInit {
  form: FormGroup;
  securityId: number;

  constructor(private api: AdminApi,
              private router: Router,
              private authService: AuthService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.securityId = Util.getIdFromRoute(this.route, 'securityId') as number;
      this.reset();
    });
  }

  reset() {
    let group: any = {
      name: new FormControl(''),
      securityType: new FormControl(''),
      sector: new FormControl(''),
      industry: new FormControl(''),
      exchange: new FormControl(''),
      currencyId: new FormControl(''),
    };
    this.form = new FormGroup(group);

    if (this.securityId != 0) {
      this.api.getSecurity(this.securityId).subscribe(security => {
        this.form.controls['name'].setValue(security.name);
        this.form.controls['securityType'].setValue(security.securityType);
        this.form.controls['sector'].setValue(security.sector);
        this.form.controls['industry'].setValue(security.industry);
        this.form.controls['exchange'].setValue(security.exchange);
        this.form.controls['currencyId'].setValue(security.currencyId);
      });
    }
  }

  onSaveButtonClicked() {
    this.api.updateSecurity(
      this.securityId,
      this.form.value.name,
      this.form.value.securityType,
      this.form.value.sector,
      this.form.value.industry,
      this.form.value.exchange,
      this.form.value.currencyId).subscribe(security => {
      this.router.navigate(['/admin/securities']);
    });
  }

  onCancelButtonClicked() {
    this.router.navigate(['/admin/security']);
  }

  onDeleteButtonClicked() {
    if (this.securityId != null) {
      this.api.deleteSecurity(this.securityId).subscribe(() => this.onCancelButtonClicked());
    } else {
      this.onCancelButtonClicked();
    }
  }

  protected readonly Util = Util;
  protected readonly CacheService = CacheService;
}
