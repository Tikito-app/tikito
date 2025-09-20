import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityHoldingGraphComponent } from './security-holding-graph.component';

describe('SecurityHoldingGraphComponent', () => {
  let component: SecurityHoldingGraphComponent;
  let fixture: ComponentFixture<SecurityHoldingGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecurityHoldingGraphComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SecurityHoldingGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
