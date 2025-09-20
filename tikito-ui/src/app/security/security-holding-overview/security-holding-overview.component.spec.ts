import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityHoldingOverviewComponent } from './security-holding-overview.component';

describe('SecurityHoldingOverviewComponent', () => {
  let component: SecurityHoldingOverviewComponent;
  let fixture: ComponentFixture<SecurityHoldingOverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecurityHoldingOverviewComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SecurityHoldingOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
