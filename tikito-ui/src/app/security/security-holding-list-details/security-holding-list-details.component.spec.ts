import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityHoldingListDetailsComponent } from './security-holding-list-details.component';

describe('SecurityHoldingListDetailsComponent', () => {
  let component: SecurityHoldingListDetailsComponent;
  let fixture: ComponentFixture<SecurityHoldingListDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecurityHoldingListDetailsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SecurityHoldingListDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
