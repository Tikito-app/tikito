import { Component, OnInit, ChangeDetectionStrategy } from "@angular/core";

@Component({
  selector: "popover-container",
  templateUrl: "./popover-container.component.html",
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ["./popover-container.component.scss"]
})
export class PopovercontainerComponent {
  constructor() {}

}
