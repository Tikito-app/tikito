import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminImportExportSettingComponent } from './admin-import-export-setting.component';

describe('AdminImportExportSettingComponent', () => {
  let component: AdminImportExportSettingComponent;
  let fixture: ComponentFixture<AdminImportExportSettingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminImportExportSettingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminImportExportSettingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
