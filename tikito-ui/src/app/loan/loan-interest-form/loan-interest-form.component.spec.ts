import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanInterestFormComponent } from './loan-interest-form.component';

describe('LoanInterestFormComponent', () => {
  let component: LoanInterestFormComponent;
  let fixture: ComponentFixture<LoanInterestFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanInterestFormComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LoanInterestFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
