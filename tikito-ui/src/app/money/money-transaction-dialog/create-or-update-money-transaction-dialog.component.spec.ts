import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateOrUpdateMoneyTransactionDialogComponent } from './create-or-update-money-transaction-dialog.component';

describe('MoneyTransactionDialogComponent', () => {
  let component: CreateOrUpdateMoneyTransactionDialogComponent;
  let fixture: ComponentFixture<CreateOrUpdateMoneyTransactionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateOrUpdateMoneyTransactionDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CreateOrUpdateMoneyTransactionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
