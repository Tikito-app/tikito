import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanPartListComponent } from './loan-part-list.component';

describe('LoanListComponent', () => {
  let component: LoanPartListComponent;
  let fixture: ComponentFixture<LoanPartListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanPartListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LoanPartListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
