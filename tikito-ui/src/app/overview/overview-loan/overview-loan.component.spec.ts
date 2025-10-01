import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OverviewLoanComponent } from './overview-loan.component';

describe('OverviewLoanComponent', () => {
  let component: OverviewLoanComponent;
  let fixture: ComponentFixture<OverviewLoanComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OverviewLoanComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OverviewLoanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
