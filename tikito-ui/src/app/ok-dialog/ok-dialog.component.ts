import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MyData} from "../ok-cancel-dialog/ok-cancel-dialog.component";
import {MatMenuModule} from "@angular/material/menu";
import {MatIconModule} from "@angular/material/icon";
import { CommonModule } from '@angular/common';
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {MatBadgeModule} from "@angular/material/badge";
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';

@Component({
  selector: 'app-ok-dialog',
  standalone: true,
  imports: [
    MatMenuModule,
    MatIconModule,
    CommonModule,
    TranslateModule,
    MatBadgeModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule],
  templateUrl: './ok-dialog.component.html',
  styleUrls: ['./ok-dialog.component.scss']
})
export class OkDialogComponent {

    constructor(
        public dialogRef: MatDialogRef<OkDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: MyData) {
    }

    onCancel() {
        this.dialogRef.close();
    }
}
