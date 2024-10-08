import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericDialogQuestionComponent } from './generic-dialog-question.component';

describe('GenericDialogQuestionComponent', () => {
  let component: GenericDialogQuestionComponent;
  let fixture: ComponentFixture<GenericDialogQuestionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GenericDialogQuestionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericDialogQuestionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
