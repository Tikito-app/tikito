import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanGraphFilterComponent } from './loan-graph-filter.component';

describe('LoanGraphFilterComponent', () => {
  let component: LoanGraphFilterComponent;
  let fixture: ComponentFixture<LoanGraphFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanGraphFilterComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LoanGraphFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
