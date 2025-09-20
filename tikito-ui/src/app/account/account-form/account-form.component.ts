import {Component, OnInit} from '@angular/core';
import {MatCard, MatCardContent, MatCardHeader, MatCardModule} from "@angular/material/card";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {NgIf} from "@angular/common";
import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";
import {Util} from "../../util";
import {TranslatePipe} from "@ngx-translate/core";
import {AccountApi} from "../../api/account-api";
import {ActivatedRoute, Router} from "@angular/router";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import {AuthService} from "../../service/auth.service";
import {CacheService} from "../../service/cache-service";

@Component({
  selector: 'app-account-form',
  standalone: true,
  imports: [
    MatCardModule,
    MatCard,
    MatCardHeader,
    MatCardContent,
    ReactiveFormsModule,
    MatLabel,
    MatFormField,
    MatIcon,
    NgIf,
    MatInput,
    MatButton,
    TranslatePipe,
    MatOption,
    MatSelect
  ],
  templateUrl: './account-form.component.html',
  styleUrl: './account-form.component.scss'
})
export class AccountFormComponent implements OnInit {
  form: FormGroup;
  accountId: number;

  constructor(private api: AccountApi,
              private router: Router,
              private authService: AuthService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.accountId = Util.getIdFromRoute(this.route, 'accountId');
      this.reset();
    });
  }

  reset() {
    let group: any = {
      name: new FormControl(''),
      accountNumber: new FormControl(''),
      accountType: new FormControl(''),
      currencyId: new FormControl(''),
    };
    this.form = new FormGroup(group);

    if (this.accountId != 0) {
      this.api.getAccount(this.accountId).subscribe(account => {
        this.form.controls['name'].setValue(account.name);
        this.form.controls['accountNumber'].setValue(account.accountNumber);
        this.form.controls['accountType'].setValue(account.accountType);
        this.form.controls['currencyId'].setValue(account.currencyId);
      });
    }
  }

  onSaveButtonClicked() {
    this.api.createOrUpdateAccount(
      this.accountId,
      this.form.value.name,
      this.form.value.accountNumber,
      this.form.value.accountType,
      this.form.value.currencyId).subscribe(account => {
      this.router.navigate(['/account']);
    })
  }

  onCancelButtonClicked() {
    this.router.navigate(['/account']);
  }

  onDeleteButtonClicked() {
    if (this.accountId != null) {
      this.api.deleteAccount(this.accountId).subscribe(() => this.onCancelButtonClicked());
    } else {
      this.onCancelButtonClicked();
    }
  }

  protected readonly Util = Util;
  protected readonly CacheService = CacheService;
}
