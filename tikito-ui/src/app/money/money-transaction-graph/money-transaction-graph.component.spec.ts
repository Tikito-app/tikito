import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MoneyTransactionGraphComponent } from './money-transaction-graph.component';

describe('MoneyTransactionGraphComponent', () => {
  let component: MoneyTransactionGraphComponent;
  let fixture: ComponentFixture<MoneyTransactionGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MoneyTransactionGraphComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MoneyTransactionGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
