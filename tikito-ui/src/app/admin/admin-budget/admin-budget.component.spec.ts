import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminBudgetComponent } from './admin-budget.component';

describe('AdminBudgetComponent', () => {
  let component: AdminBudgetComponent;
  let fixture: ComponentFixture<AdminBudgetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminBudgetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminBudgetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
