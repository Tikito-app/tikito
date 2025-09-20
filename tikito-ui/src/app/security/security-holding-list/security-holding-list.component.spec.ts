import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityHoldingListComponent } from './security-holding-list.component';

describe('SecurityHoldingListComponent', () => {
  let component: SecurityHoldingListComponent;
  let fixture: ComponentFixture<SecurityHoldingListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecurityHoldingListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SecurityHoldingListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
