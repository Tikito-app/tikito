import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityTransactionListComponent } from './security-transaction-list.component';

describe('SecurityTransactionListComponent', () => {
  let component: SecurityTransactionListComponent;
  let fixture: ComponentFixture<SecurityTransactionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecurityTransactionListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SecurityTransactionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
