import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AggregatedSecurityHoldingsGraphComponent } from './aggregated-security-holdings-graph.component';

describe('AggregatedSecurityHoldingsGraphComponent', () => {
  let component: AggregatedSecurityHoldingsGraphComponent;
  let fixture: ComponentFixture<AggregatedSecurityHoldingsGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AggregatedSecurityHoldingsGraphComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AggregatedSecurityHoldingsGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
