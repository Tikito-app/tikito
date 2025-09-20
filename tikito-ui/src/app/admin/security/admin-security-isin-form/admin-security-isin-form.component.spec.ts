import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminSecurityIsinFormComponent } from './admin-security-isin-form.component';

describe('AdminSecurityIsinFormComponent', () => {
  let component: AdminSecurityIsinFormComponent;
  let fixture: ComponentFixture<AdminSecurityIsinFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminSecurityIsinFormComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminSecurityIsinFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
