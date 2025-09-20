import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MoneyTransactionGroupListComponent } from './money-transaction-group-list.component';

describe('MoneyTransactionGroupListComponent', () => {
  let component: MoneyTransactionGroupListComponent;
  let fixture: ComponentFixture<MoneyTransactionGroupListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MoneyTransactionGroupListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MoneyTransactionGroupListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
