import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SetTransactionGroupDialogComponent } from './set-transaction-group-dialog.component';

describe('SetTransactionGroupDialogComponent', () => {
  let component: SetTransactionGroupDialogComponent;
  let fixture: ComponentFixture<SetTransactionGroupDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SetTransactionGroupDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SetTransactionGroupDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
