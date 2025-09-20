import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MoneyTransactionOverviewComponent } from './money-transaction-overview.component';

describe('MoneyTransactionOverviewComponent', () => {
  let component: MoneyTransactionOverviewComponent;
  let fixture: ComponentFixture<MoneyTransactionOverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MoneyTransactionOverviewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MoneyTransactionOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
