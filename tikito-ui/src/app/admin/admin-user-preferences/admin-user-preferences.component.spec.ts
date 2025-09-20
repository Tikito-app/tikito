import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminUserPreferencesComponent } from './admin-user-preferences.component';

describe('AdminUserPreferencesComponent', () => {
  let component: AdminUserPreferencesComponent;
  let fixture: ComponentFixture<AdminUserPreferencesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminUserPreferencesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminUserPreferencesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
