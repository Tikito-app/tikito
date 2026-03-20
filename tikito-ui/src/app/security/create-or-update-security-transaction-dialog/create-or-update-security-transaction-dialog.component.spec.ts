import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateOrUpdateSecurityTransactionDialogComponent } from './create-or-update-security-transaction-dialog.component';

describe('SecurityTransactionDialogComponent', () => {
  let component: CreateOrUpdateSecurityTransactionDialogComponent;
  let fixture: ComponentFixture<CreateOrUpdateSecurityTransactionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateOrUpdateSecurityTransactionDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CreateOrUpdateSecurityTransactionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
