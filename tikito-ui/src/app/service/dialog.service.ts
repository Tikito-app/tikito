import {Injectable} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {OkCancelDialogComponent} from "../ok-cancel-dialog/ok-cancel-dialog.component";
import {OkDialogComponent} from "../ok-dialog/ok-dialog.component";
import {MatSnackBar} from "@angular/material/snack-bar";
import {
  SetTransactionGroupDialogComponent
} from "../money/set-transaction-group-dialog/set-transaction-group-dialog.component";
import MoneyTransactionGroup from "../dto/money/money-transaction-group";
import {TranslateService} from "./translate.service";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  defaultSnackbareTimeout = 5000;

  constructor(private dialog: MatDialog,
              private _snackbar: MatSnackBar,
              private translateService: TranslateService) {
  }

  okCancel(title: string, message: string): Promise<boolean> {
    return new Promise<boolean>(resolve => {
      const dialogRef = this.dialog.open(OkCancelDialogComponent, {
        width: '250px',
        data: {title: title, message: message},
      });
      dialogRef.afterClosed().subscribe(value => {
        if (value == null) {
          resolve(false);
        }
        resolve(true);
      });
    });
  }

  setTransactionGroup(selectedGroupId: number): Promise<MoneyTransactionGroup | null> {
    return new Promise<MoneyTransactionGroup | null>(resolve => {
      const dialogRef = this.dialog.open(SetTransactionGroupDialogComponent, {
        width: '450px',
        data: {selectedGroupId: selectedGroupId}
      });
      dialogRef.afterClosed().subscribe(value => {
        if (value == null) {
          resolve(null);
        }
        resolve(value);
      });
    });
  }

  snackbar(message: string, close?: string, timeoutToClose?: number) {
    let snackbar = this._snackbar.open(message, close);

    setTimeout(() => {
      snackbar.dismissWithAction();
    }, timeoutToClose == null ? this.defaultSnackbareTimeout : timeoutToClose)
  }

  ok(title: string, message: string): Promise<void> {
    return new Promise<void>(resolve => {
      const dialogRef = this.dialog.open(OkDialogComponent, {
        width: '250px',
        data: {title: title, message: message},
      });
      dialogRef.afterClosed().subscribe(value => {
        resolve();
      });
    })
  }

  deleteConfirmation(): Observable<void> {
    return new Observable(subscriber => {
      this.okCancel(
        this.translateService.translate('are-you-sure-delete-title'),
        this.translateService.translate('are-you-sure-delete-text'))
        .then((doDelete) => {
          if (doDelete) {
            subscriber.next();
          }
        });
    });
  }
}
