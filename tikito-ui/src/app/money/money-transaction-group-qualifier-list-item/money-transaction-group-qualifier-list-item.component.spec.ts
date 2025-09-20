import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MoneyTransactionGroupQualifierListItemComponent } from './money-transaction-group-qualifier-list-item.component';

describe('MoneyTransactionGroupQualifierListItemComponent', () => {
  let component: MoneyTransactionGroupQualifierListItemComponent;
  let fixture: ComponentFixture<MoneyTransactionGroupQualifierListItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MoneyTransactionGroupQualifierListItemComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MoneyTransactionGroupQualifierListItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
