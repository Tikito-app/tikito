import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MoneyTransactionGroupFormComponent } from './money-transaction-group-form.component';

describe('MoneyTransactionGroupFormComponent', () => {
  let component: MoneyTransactionGroupFormComponent;
  let fixture: ComponentFixture<MoneyTransactionGroupFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MoneyTransactionGroupFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MoneyTransactionGroupFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
