import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanMoneyTransactionsListComponent } from './loan-money-transactions-list.component';

describe('LoanMoneyTransactionsListComponent', () => {
  let component: LoanMoneyTransactionsListComponent;
  let fixture: ComponentFixture<LoanMoneyTransactionsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanMoneyTransactionsListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LoanMoneyTransactionsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
