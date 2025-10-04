import {Component} from '@angular/core';
import {TranslatePipe} from "@ngx-translate/core";
import {MatAnchor} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {Router} from "@angular/router";

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    TranslatePipe,
    MatAnchor,
    MatIcon
  ],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss'
})
export class AdminComponent {

  constructor(private router: Router) {
  }

  routeToAdminUserPreferences() {
    this.router.navigate(['admin/user-preferences']);
  }

  routeToAdminUsers() {
    this.router.navigate(['admin/users']);
  }

  routeToAdminSecurity() {
    this.router.navigate(['admin/security']);
  }

  routeToAdminMoney() {
    this.router.navigate(['admin/money']);
  }

  routeToAdminBudget() {
    this.router.navigate(['admin/budget']);
  }

  routeToExport() {
    this.router.navigate(['admin/export']);
  }

  routeToImport() {
    this.router.navigate(['admin/import']);
  }
}
