import {Component} from '@angular/core';
import {MatButton} from "@angular/material/button";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatOption, provideNativeDateAdapter} from "@angular/material/core";
import {MatSelect} from "@angular/material/select";
import {NgIf} from "@angular/common";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {TranslatePipe} from "@ngx-translate/core";
import {AuthService} from "../../service/auth.service";
import {CacheService} from "../../service/cache-service";
import {UserPreferenceService} from "../../service/user-preference-service";
import {UserPreference} from "../../dto/user-preference";
import {Util} from "../../util";
import {MatIcon} from "@angular/material/icon";
import {Router} from "@angular/router";

@Component({
  selector: 'app-admin-user-preferences',
  standalone: true,
  imports: [
    MatButton,
    MatCard,
    MatCardContent,
    MatCardHeader,
    MatCardTitle,
    MatCheckbox,
    MatFormField,
    MatInput,
    MatLabel,
    MatOption,
    MatSelect,
    NgIf,
    ReactiveFormsModule,
    TranslatePipe,
    MatIcon
  ],
  templateUrl: './admin-user-preferences.component.html',
  styleUrl: './admin-user-preferences.component.scss',
  providers: [provideNativeDateAdapter(), TranslatePipe]
})
export class AdminUserPreferencesComponent {
  form: FormGroup;

  constructor(private authService: AuthService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.form = new FormGroup({
        defaultCurrency: new FormControl(),
        overviewStartDateMinusAmount: new FormControl(),
        overviewStartDateMinusUnit: new FormControl(),
        overviewStartDateAtBeginningOfRange: new FormControl(),
      });

      this.form.controls['defaultCurrency'].setValue(UserPreferenceService.get<string>(UserPreference.DEFAULT_CURRENCY, 'EUR'));
      this.form.controls['overviewStartDateMinusAmount'].setValue(UserPreferenceService.get<number>(UserPreference.OVERVIEW_START_DATE_MINUS_AMOUNT, 1));
      this.form.controls['overviewStartDateMinusUnit'].setValue(UserPreferenceService.get<string>(UserPreference.OVERVIEW_START_DATE_MINUS_UNIT, 'YEAR'));
      this.form.controls['overviewStartDateAtBeginningOfRange'].setValue(UserPreferenceService.get<boolean>(UserPreference.OVERVIEW_START_DATE_AT_BEGINNING_OF_RANGE, true));
    });
  }

  routeToAdmin() {
    this.router.navigate(['/admin']);
  }

  protected readonly CacheService = CacheService;


  protected readonly Util = Util;
  protected readonly UserPreferenceService = UserPreferenceService;
  protected readonly UserPreference = UserPreference;
}
