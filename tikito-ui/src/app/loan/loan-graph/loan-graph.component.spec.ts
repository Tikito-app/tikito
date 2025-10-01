import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanGraphComponent } from './loan-graph.component';

describe('LoanGraphComponent', () => {
  let component: LoanGraphComponent;
  let fixture: ComponentFixture<LoanGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanGraphComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LoanGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
