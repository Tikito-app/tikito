import {Component, Input, ChangeDetectionStrategy} from '@angular/core';
import {Router} from "@angular/router";
import {MatAnchor} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";


@Component({
    selector: 'app-header-item',
    imports: [
    MatAnchor,
    MatIcon
],
    templateUrl: './header-item.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
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
