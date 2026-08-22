import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityLogoComponent } from './security-logo.component';

describe('LogoComponent', () => {
  let component: SecurityLogoComponent;
  let fixture: ComponentFixture<SecurityLogoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SecurityLogoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SecurityLogoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
