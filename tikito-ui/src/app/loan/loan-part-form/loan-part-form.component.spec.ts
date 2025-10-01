import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanPartFormComponent } from './loan-part-form.component';

describe('LoanFormComponent', () => {
  let component: LoanPartFormComponent;
  let fixture: ComponentFixture<LoanPartFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoanPartFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LoanPartFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
