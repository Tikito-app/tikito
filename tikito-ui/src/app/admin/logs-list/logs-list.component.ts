import {Component, OnInit} from '@angular/core';
import {LogApiService} from '../../api/log-api.service';
import {Log} from '../../dto/log';
import {CommonModule} from '@angular/common';
import {MatTableModule} from '@angular/material/table';
import {MatCardModule} from '@angular/material/card';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {TranslatePipe} from "@ngx-translate/core";
import {DialogService} from "../../service/dialog.service";
import {Router} from "@angular/router";
import {AuthService} from "../../service/auth.service";

@Component({
  selector: 'app-logs-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatCardModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    TranslatePipe
  ],
  templateUrl: './logs-list.component.html',
  styleUrl: './logs-list.component.scss'
})
export class LogsListComponent implements OnInit {
  logs: Log[] = [];
  displayedColumns: string[] = ['timestamp', 'objectIdentifier', 'message', 'actions'];

  constructor(
    private logApiService: LogApiService,
    private dialogService: DialogService,
    private router: Router,
    private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady(() => {
      this.reset();
    });
  }

  reset(): void {
    this.logApiService.getLogs().subscribe((data: Log[]) => {
      this.logs = data;
    });
  }

  deleteLog(id: number): void {
    this.dialogService.deleteConfirmation().subscribe(() => {
      this.logApiService.deleteLog(id).subscribe(() => {
        this.reset();
      });
    });
  }

  routeToAdmin() {
    this.router.navigate(['/admin']);
  }
}
