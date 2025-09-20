import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MoneyImportResultComponent } from './money-import-result.component';

describe('MoneyImportResultComponent', () => {
  let component: MoneyImportResultComponent;
  let fixture: ComponentFixture<MoneyImportResultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MoneyImportResultComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MoneyImportResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
