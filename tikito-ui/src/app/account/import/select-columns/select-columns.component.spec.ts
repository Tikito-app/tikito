import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectColumnsComponent } from './select-columns.component';

describe('SelectColumnsComponent', () => {
  let component: SelectColumnsComponent;
  let fixture: ComponentFixture<SelectColumnsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectColumnsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SelectColumnsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
