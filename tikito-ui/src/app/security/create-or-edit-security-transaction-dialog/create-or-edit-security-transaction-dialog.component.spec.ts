import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateOrEditSecurityTransactionDialogComponent } from './create-or-edit-security-transaction-dialog.component';

describe('SecurityTransactionDialogComponent', () => {
  let component: CreateOrEditSecurityTransactionDialogComponent;
  let fixture: ComponentFixture<CreateOrEditSecurityTransactionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateOrEditSecurityTransactionDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CreateOrEditSecurityTransactionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
