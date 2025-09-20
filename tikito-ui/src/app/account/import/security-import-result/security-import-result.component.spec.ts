import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityImportResultComponent } from './security-import-result.component';

describe('SecurityImportResultComponent', () => {
  let component: SecurityImportResultComponent;
  let fixture: ComponentFixture<SecurityImportResultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecurityImportResultComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SecurityImportResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
