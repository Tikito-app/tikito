import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanInterestListItemComponent } from './loan-interest-list-item.component';

describe('LoanInterestListItemComponent', () => {
  let component: LoanInterestListItemComponent;
  let fixture: ComponentFixture<LoanInterestListItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanInterestListItemComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LoanInterestListItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
