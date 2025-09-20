import {Component, Input} from '@angular/core';
import {MatIcon} from "@angular/material/icon";
import {MatSuffix} from "@angular/material/form-field";
import {PopoverModule} from "./popover.module";
import {TranslatePipe} from "@ngx-translate/core";

@Component({
  selector: 'app-popover',
  standalone: true,
  imports: [
    MatIcon,
    MatSuffix,
    PopoverModule,
    TranslatePipe
  ],
  templateUrl: './popover.component.html',
  styleUrl: './popover.component.scss'
})
export class PopoverComponent {
  @Input()
  text: string;
}
