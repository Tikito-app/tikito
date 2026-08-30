import {Component, Input, ChangeDetectionStrategy} from '@angular/core';
import {Router} from "@angular/router";
import {MatAnchor, MatButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";


@Component({
    selector: 'app-header-item',
  imports: [
    MatAnchor,
    MatIcon,
    MatButton
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

  @Input()
  active: boolean = false;

  constructor(
    private router: Router) {
  }

  routeTo() {
    this.router.navigate([this.routerLink]);
  }
}
