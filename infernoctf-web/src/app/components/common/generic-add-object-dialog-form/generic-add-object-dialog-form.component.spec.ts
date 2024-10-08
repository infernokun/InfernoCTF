import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericAddObjectDialogFormComponent } from './generic-add-object-dialog-form.component';

describe('GenericAddObjectDialogFormComponent', () => {
  let component: GenericAddObjectDialogFormComponent;
  let fixture: ComponentFixture<GenericAddObjectDialogFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GenericAddObjectDialogFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericAddObjectDialogFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
