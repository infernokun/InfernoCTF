import {
  Component,
  ElementRef,
  Input,
  OnInit,
  ViewChild,
} from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { QuestionBase } from '../../../models/SimpleFormData.model';
import { RoomFormData } from '../../../models/room.model';

@Component({
  selector: 'app-generic-dialog-question',
  templateUrl: './generic-dialog-question.component.html',
  styleUrls: ['./generic-dialog-question.component.scss'],
  standalone: false
})
export class GenericDialogQuestionComponent implements OnInit {
  @Input() question!: QuestionBase;

  @ViewChild('dropdownMenu')
  dropdownMenu!: ElementRef;

  @ViewChild('textInput')
  textInput!: ElementRef;

  formControl: FormControl = new FormControl('');

  required: string[] = [
    'title',
    'rank',
    'definition',
    'status',
    'statusNarrative',
    'information',
    'source',
    'link',
  ];

  constructor(private dialog: MatDialog) { }

  ngOnInit(): void {
    this.formControl.valueChanges.subscribe((value) => {
      this.question.cb(this.question.key, value);
    });
    if (this.question.dependentQuestions) {
      for (const ent of this.question.dependentQuestions.entries()) {
        ent[1].cb = this.question.cb;
      }
    }

    if (this.required.includes(this.question.key)) {
      this.formControl.setValidators([Validators.required]);
    }

    if (this.question.value) {

      this.formControl.setValue(this.question.value.toString());
      return;
    }
    if (this.question.type === 'dropdown') {
      this.question.cb(this.question.key, this.question.options[0].value);
    }
    if (this.question.type === 'number') {
      this.formControl.addValidators(Validators.pattern(/^[0-9]*$/));
    }
  }

  handleAction(): void {
    switch (this.question.key) {
      case 'classification':
        const config = new MatDialogConfig();
        config.autoFocus = false;
        config.minWidth = "50vw";
        config.minHeight = "10vw";
        config.data = {}; //title: 'Classification Picker', existingClassification: this.question.value, classificationOptions: this.environmentSvc.settings.classificationOptions
        if (this.question.action) {
          this.question.action(config).subscribe((result: RoomFormData) => {
            if (result) {
              this.formControl.setValue(result.questions);
            }
          });
        }
        break;
    }
  }
}

