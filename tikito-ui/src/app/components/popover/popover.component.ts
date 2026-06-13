import {Component, Input, ChangeDetectionStrategy} from '@angular/core';
import {MatIcon} from "@angular/material/icon";
import {MatSuffix} from "@angular/material/form-field";
import {PopoverModule} from "./popover.module";
import {TranslatePipe} from "@ngx-translate/core";

@Component({
    selector: 'app-popover',
    imports: [
        MatIcon,
        MatSuffix,
        PopoverModule,
        TranslatePipe
    ],
    templateUrl: './popover.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './popover.component.scss'
})
export class PopoverComponent {
  @Input()
  text: string;
}
