import {Component, Input} from '@angular/core';
import {Router} from "@angular/router";
import {MatAnchor} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-header-item',
  standalone: true,
  imports: [
    MatAnchor,
    MatIcon,
    NgIf
  ],
  templateUrl: './header-item.component.html',
  styleUrl: './header-item.component.scss'
})
export class HeaderItemComponent {
  @Input()
  routerLink: string;

  @Input()
  displayValue: string;

  @Input()
  icon: string;

  constructor(
    private router: Router) {
  }

  routeTo() {
    this.router.navigate([this.routerLink]);
  }
}
