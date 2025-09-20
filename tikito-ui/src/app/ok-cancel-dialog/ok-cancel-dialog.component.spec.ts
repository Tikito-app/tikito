import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OkCancelDialogComponent } from './ok-cancel-dialog.component';

describe('OkCancelDialogComponent', () => {
  let component: OkCancelDialogComponent;
  let fixture: ComponentFixture<OkCancelDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OkCancelDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OkCancelDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
