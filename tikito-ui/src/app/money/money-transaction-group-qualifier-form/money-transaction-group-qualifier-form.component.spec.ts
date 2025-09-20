import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MoneyTransactionGroupQualifierFormComponent } from './money-transaction-group-qualifier-form.component';

describe('MoneyTransactionGroupQualifierFormTestComponent', () => {
  let component: MoneyTransactionGroupQualifierFormComponent;
  let fixture: ComponentFixture<MoneyTransactionGroupQualifierFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MoneyTransactionGroupQualifierFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MoneyTransactionGroupQualifierFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
