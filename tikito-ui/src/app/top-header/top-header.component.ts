import {Component, OnInit} from '@angular/core';
import {HeaderItemComponent} from "../header-item/header-item.component";
import {NgIf} from "@angular/common";
import {TranslatePipe} from "@ngx-translate/core";
import {Util} from "../util";
import {AccountType} from "../dto/account-type";
import {AuthService} from "../service/auth.service";

@Component({
  selector: 'app-top-header',
  standalone: true,
  imports: [
    HeaderItemComponent,
    TranslatePipe,
    NgIf,
  ],
  templateUrl: './top-header.component.html',
  styleUrl: './top-header.component.scss'
})
export class TopHeaderComponent implements OnInit {
  loggedIn: boolean;
  initialInstallation: boolean;

  constructor(private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.loggedIn = loggedInUser != null;
    });

    this.initialInstallation = window.location.href.endsWith('/initial-installation');

    this.authService.onUserLoggedIn((user: any) => this.loggedIn = true);
    this.authService.onUserLoggedOut(() => this.loggedIn = false);
  }

  protected readonly Util = Util;
  protected readonly AccountType = AccountType;
}
