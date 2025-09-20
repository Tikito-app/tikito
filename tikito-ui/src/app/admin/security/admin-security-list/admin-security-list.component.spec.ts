import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminSecurityListComponent } from './admin-security-list.component';

describe('AdminSecurityListComponent', () => {
  let component: AdminSecurityListComponent;
  let fixture: ComponentFixture<AdminSecurityListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminSecurityListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminSecurityListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
