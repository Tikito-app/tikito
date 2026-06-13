import {Component, EventEmitter, Input, OnChanges, Output, ViewChild} from '@angular/core';
import SecurityHolding from "../../dto/security/security-holding";
import {SecurityUtil} from "../../security-util";
import {MatIcon} from "@angular/material/icon";
import {TranslatePipe} from "@ngx-translate/core";
import {MatDivider} from "@angular/material/divider";
import {MatList, MatListItem} from "@angular/material/list";
import {MatButton} from "@angular/material/button";
import {Router} from "@angular/router";
import {SecurityHoldingGraphComponent} from "../security-holding-graph/security-holding-graph.component";
import {SecurityHoldingGraphDisplayField} from "../../dto/security/security-holding-graph-display-field";
import {CurrencyComponent} from "../../components/currency/currency.component";
import {CacheService} from "../../service/cache-service";
import {SecurityHoldingFilter} from "../../dto/security/security-holding-filter";
import {PercentageComponent} from "../../components/percentage/percentage.component";

@Component({
    selector: 'app-security-holding-list-details',
    imports: [
        MatIcon,
        MatDivider,
        MatList,
        MatListItem,
        MatButton,
        SecurityHoldingGraphComponent,
        CurrencyComponent,
        PercentageComponent,
        TranslatePipe
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

  constructor(private router: Router) {
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

  mapAccountIds(holding: SecurityHolding): string {
    return CacheService.getAccountById(holding.accountId).name;
  }

  getSecurityHoldingFilter() {
    let filter = new SecurityHoldingFilter();
    filter.securityIds = this.holding.securityId == null ? [] : [this.holding.securityId];
    filter.accountIds = this.holding.accountId == null ? [] : [this.holding.accountId];
    filter.displayField = SecurityHoldingGraphDisplayField.PRICE;
    return filter;
  }

  protected readonly SecurityUtil = SecurityUtil;
}
