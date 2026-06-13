import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {MatMenuModule} from "@angular/material/menu";
import {MatIconModule} from "@angular/material/icon";
import {CommonModule} from '@angular/common';
import {MatBadgeModule} from "@angular/material/badge";
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {TranslatePipe} from "@ngx-translate/core";

export interface MyData {
  title: string,
  message: string
}

@Component({
    selector: 'app-ok-cancel-dialog',
    imports: [
        MatMenuModule,
        MatIconModule,
        CommonModule,
        MatBadgeModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatDialogModule,
        TranslatePipe
    ],
    templateUrl: './ok-cancel-dialog.component.html',
    styleUrls: ['./ok-cancel-dialog.component.scss']
})
export class OkCancelDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<OkCancelDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MyData) {
  }

  onCancel() {
    this.dialogRef.close();
  }

}
