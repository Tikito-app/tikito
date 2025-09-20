import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MoneyTransactionListComponent } from './money-transaction-list.component';

describe('MoneyTransactionListComponent', () => {
  let component: MoneyTransactionListComponent;
  let fixture: ComponentFixture<MoneyTransactionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MoneyTransactionListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MoneyTransactionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
