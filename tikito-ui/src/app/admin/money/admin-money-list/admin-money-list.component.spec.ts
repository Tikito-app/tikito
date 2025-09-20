import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminMoneyListComponent } from './admin-money-list.component';

describe('AdminMoneyListComponent', () => {
  let component: AdminMoneyListComponent;
  let fixture: ComponentFixture<AdminMoneyListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminMoneyListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminMoneyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
