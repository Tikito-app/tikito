import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InitialInstallationComponent } from './initial-installation.component';

describe('InitialInstallationComponent', () => {
  let component: InitialInstallationComponent;
  let fixture: ComponentFixture<InitialInstallationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InitialInstallationComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(InitialInstallationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
