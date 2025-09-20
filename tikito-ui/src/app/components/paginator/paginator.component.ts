import {Component, ViewChild} from '@angular/core';
import {MatPaginator} from "@angular/material/paginator";
import {Util} from "../../util";

@Component({
  selector: 'app-paginator',
  standalone: true,
  imports: [
    MatPaginator
  ],
  templateUrl: './paginator.component.html',
  styleUrl: './paginator.component.scss'
})
export class PaginatorComponent {
  @ViewChild(MatPaginator) paginator: MatPaginator;

  public getPaginator(): MatPaginator {
    return this.paginator;
  }

  protected readonly Util = Util;
}
