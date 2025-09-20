import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityPieGraphComponent } from './security-pie-graph.component';

describe('TradingCompanyPieGraphComponent', () => {
  let component: SecurityPieGraphComponent;
  let fixture: ComponentFixture<SecurityPieGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecurityPieGraphComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecurityPieGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
