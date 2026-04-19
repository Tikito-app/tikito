import {Component} from '@angular/core';
import {MatAnchor} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {Router} from "@angular/router";
import {TranslatePipe} from "../../service/translate-pipe.pipe";

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    MatAnchor,
    MatIcon,
    TranslatePipe
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

  routeToExport() {
    this.router.navigate(['admin/export']);
  }

  routeToImport() {
    this.router.navigate(['admin/import']);
  }
}
