import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-security-logo',
  imports: [],
  templateUrl: './security-logo.component.html',
  styleUrl: './security-logo.component.scss',
})
export class SecurityLogoComponent {
  @Input()
  isin: string;

  @Input()
  size: number = 40;
}
