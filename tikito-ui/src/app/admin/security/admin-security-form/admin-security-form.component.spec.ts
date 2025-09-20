import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminSecurityFormComponent } from './admin-security-form.component';

describe('AdminSecurityFormComponent', () => {
  let component: AdminSecurityFormComponent;
  let fixture: ComponentFixture<AdminSecurityFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminSecurityFormComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminSecurityFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
