import {Component, Inject, OnInit} from '@angular/core';
import {MatButton} from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from "@angular/material/dialog";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {TranslatePipe} from "@ngx-translate/core";
import {MoneyApi} from "../../api/money-api";
import {AuthService} from "../../service/auth.service";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {MatOption} from "@angular/material/core";
import {MatSelect} from "@angular/material/select";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";

export interface MyData {
  selectedGroupId: number
}

@Component({
  selector: 'app-set-transaction-group-dialog',
  standalone: true,
  imports: [
    MatButton,
    MatDialogActions,
    MatDialogContent,
    MatDialogTitle,
    MatLabel,
    TranslatePipe,
    MatFormField,
    MatOption,
    MatSelect,
    ReactiveFormsModule
  ],
  templateUrl: './set-transaction-group-dialog.component.html',
  styleUrl: './set-transaction-group-dialog.component.scss'
})
export class SetTransactionGroupDialogComponent implements OnInit {
  groups: MoneyTransactionGroup[];
  form: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<SetTransactionGroupDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MyData,
    private authService: AuthService,
    private api: MoneyApi) {
  }

  ngOnInit(): void {
    this.form = new FormGroup({
      group: new FormControl(),
    });

    this.authService.onSystemReady((loggedInUser: any) => {
      this.api.getMoneyTransactionGroups().subscribe(groups => {
        let nonGroup = new MoneyTransactionGroup();
        nonGroup.id = -1;
        nonGroup.name = 'None';
        this.groups = [nonGroup, ...groups];

        let selectedGroup = groups.filter(group => group.id == this.data.selectedGroupId);
        if (selectedGroup.length == 1) {
          this.form.controls['group'].setValue(selectedGroup[0]);
        }
      })
    });
  }

  onCancel() {
    this.dialogRef.close();
  }

  onSave() {
    this.dialogRef.close(this.form.value.group);
  }
}
