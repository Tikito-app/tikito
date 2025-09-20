import {Component, EventEmitter, Input, OnChanges, Output, ViewChild} from '@angular/core';
import SecurityHolding from "../../dto/security/security-holding";
import {SecurityUtil} from "../../security-util";
import {MatIcon} from "@angular/material/icon";
import {TranslatePipe} from "@ngx-translate/core";
import {MatDivider} from "@angular/material/divider";
import {MatList, MatListItem} from "@angular/material/list";
import {MatButton} from "@angular/material/button";
import {Router} from "@angular/router";
import {SecurityApi} from "../../api/security-api";
import {SecurityHoldingGraphComponent} from "../security-holding-graph/security-holding-graph.component";
import {SecurityHoldingGraphDisplayField} from "../../dto/security/security-holding-graph-display-field";
import {CurrencyComponent} from "../../components/currency/currency.component";
import {CacheService} from "../../service/cache-service";
import {SecurityHoldingFilter} from "../../dto/security/security-holding-filter";
import {PercentageComponent} from "../../components/percentage/percentage.component";

@Component({
  selector: 'app-security-holding-list-details',
  standalone: true,
  imports: [
    MatIcon,
    TranslatePipe,
    MatDivider,
    MatList,
    MatListItem,
    MatButton,
    SecurityHoldingGraphComponent,
    CurrencyComponent,
    PercentageComponent
  ],
  templateUrl: './security-holding-list-details.component.html',
  styleUrl: './security-holding-list-details.component.scss'
})
export class SecurityHoldingListDetailsComponent implements OnChanges {

  @Input()
  holding: SecurityHolding;

  @Output()
  closeCallback: EventEmitter<void> = new EventEmitter();

  @ViewChild(SecurityHoldingGraphComponent)
  child!: SecurityHoldingGraphComponent;

  constructor(private router: Router,
              private api: SecurityApi) {
  }

  ngOnChanges(changes: any): void {
    if (!changes.holding.firstChange) {
      // only logged upon a change after rendering
      this.child.setSecurityHoldingFilter(this.getSecurityHoldingFilter());
      this.child.resetGraph();
    }
  }

  onCloseButtonClicked() {
    this.closeCallback.emit();
  }

  onDetailsButtonClicked() {
    this.router.navigate(['security-holding'], {fragment: 'holdingIds=' + this.holding.id});
  }

  mapAccountIds(holding: SecurityHolding) {
    return holding.accountIds.map(accountId => CacheService.getAccountById(accountId).name).join(',');
  }

  getSecurityHoldingFilter() {
    let filter = new SecurityHoldingFilter();
    filter.holdingIds = [this.holding.id];
    filter.displayField = SecurityHoldingGraphDisplayField.PRICE;
    return filter;
  }

  protected readonly SecurityUtil = SecurityUtil;
}
