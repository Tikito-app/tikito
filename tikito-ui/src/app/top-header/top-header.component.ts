import {Component, OnInit} from '@angular/core';
import {HeaderItemComponent} from "../header-item/header-item.component";
import {NgIf} from "@angular/common";
import {Util} from "../util";
import {AuthService} from "../service/auth.service";
import {TranslatePipe} from "../service/translate-pipe.pipe";
import {AdminApi} from "../api/admin-api";

@Component({
  selector: 'app-top-header',
  standalone: true,
  imports: [
    HeaderItemComponent,
    NgIf,
    TranslatePipe,
  ],
  templateUrl: './top-header.component.html',
  styleUrl: './top-header.component.scss'
})
export class TopHeaderComponent implements OnInit {
  loggedIn: boolean;
  initialInstallation: boolean;
  jobsPending: number = 0;

  constructor(private authService: AuthService,
              private adminApi: AdminApi) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.loggedIn = loggedInUser != null;
      this.updateJobsCount();
    });

    this.initialInstallation = window.location.href.endsWith('/initial-installation');

    this.authService.onUserLoggedIn((user: any) => this.loggedIn = true);
    this.authService.onUserLoggedOut(() => this.loggedIn = false);
  }

  updateJobsCount() {
    setTimeout(() => {
      this.adminApi.getJobsCount().subscribe(jobsPending => {
        this.jobsPending = jobsPending;
        this.updateJobsCount();
      })
    }, 5000);
  }

  protected readonly Util = Util;
}
