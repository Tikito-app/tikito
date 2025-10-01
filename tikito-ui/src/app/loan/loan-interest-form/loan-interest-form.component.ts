import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../service/auth.service";
import {LoanInterest} from "../../dto/loan-interest";
import {LoanApi} from "../../api/loan-api";
import {MatButton} from "@angular/material/button";
import {MatFormField, MatHint, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {provideNativeDateAdapter} from "@angular/material/core";
import {TranslatePipe} from "@ngx-translate/core";
import {Util} from "../../util";
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {NgIf} from "@angular/common";
import {DialogService} from "../../service/dialog.service";

@Component({
  selector: 'app-loan-interest-form',
  standalone: true,
  imports: [
    FormsModule,
    MatButton,
    MatFormField,
    MatIcon,
    MatInput,
    MatLabel,
    ReactiveFormsModule,
    TranslatePipe,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatHint,
    MatSuffix,
    NgIf
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './loan-interest-form.component.html',
  styleUrl: './loan-interest-form.component.scss'
})
export class LoanInterestFormComponent implements OnInit {
  form: FormGroup;

  @Input()
  interest: LoanInterest;

  @Output()
  callback: EventEmitter<LoanInterest | null> = new EventEmitter();

  @Output()
  deleteCallback: EventEmitter<void> = new EventEmitter();

  constructor(private authService: AuthService,
              private dialogService: DialogService) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.reset();
    });
  }

  reset() {
    let formGroup: any = {
      startDate: new FormControl(''),
      endDate: new FormControl(''),
      amount: new FormControl(''),
    };
    this.form = new FormGroup(formGroup);

    this.form.controls['startDate'].setValue(this.interest.startDate);
    this.form.controls['endDate'].setValue(this.interest.endDate);
    this.form.controls['amount'].setValue(this.interest.amount);
  }

  onSaveButtonClicked() {
    this.interest.startDate = Util.toLocalDate(this.form.value['startDate']);
    this.interest.endDate = Util.toLocalDate(this.form.value['endDate']);
    this.interest.amount = this.form.value['amount'];
    this.callback.next(new LoanInterest());
  }

  onCancelButtonClicked() {
    this.callback.next(null);
  }

  onDeleteButtonClicked() {
    this.dialogService.deleteConfirmation().subscribe(() => {
      this.deleteCallback.next();
    });
  }

  protected readonly Util = Util;
}
