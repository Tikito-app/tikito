import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminSecurityIsinListComponent } from './admin-security-isin-list.component';

describe('AdminSecurityIsinListComponent', () => {
  let component: AdminSecurityIsinListComponent;
  let fixture: ComponentFixture<AdminSecurityIsinListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminSecurityIsinListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminSecurityIsinListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
