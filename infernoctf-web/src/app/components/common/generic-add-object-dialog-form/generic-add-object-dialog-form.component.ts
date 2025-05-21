import {
  AfterViewInit,
  Component,
  Inject,
  OnInit,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { GenericDialogQuestionComponent } from '../generic-dialog-question/generic-dialog-question.component';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { SimpleFormData } from '../../../models/SimpleFormData.model';
import { MessageService } from '../../../services/message.service';

@Component({
  selector: 'app-generic-add-object-dialog-form',
  templateUrl: './generic-add-object-dialog-form.component.html',
  styleUrls: ['./generic-add-object-dialog-form.component.scss'],
  standalone: false
})
export class GenericAddObjectDialogFormComponent implements OnInit, AfterViewInit {
  // @Input() d!: SimpleFormData;
  // @Input() labelAddOn: string = "";
  // @Output() result = new EventEmitter<SimpleFormData>();

  @ViewChildren(GenericDialogQuestionComponent) questionComponents: QueryList<GenericDialogQuestionComponent> | undefined;

  dynamicForm: FormGroup | undefined;

  constructor(
    public dialogRef: MatDialogRef<GenericAddObjectDialogFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: SimpleFormData,
    private fb: FormBuilder,
    private messageService: MessageService
  ) { }

  ngOnInit(): void {
    this.data.questions.forEach((q) => {
      q.cb = (k: string, v: string) => {
        // console.log(k, v);
        this.updateMap(k, v);
      };

      if (this.data.preFilledData?.has(q.key)) {
        q.value = this.data.preFilledData?.get(q.key);
      }
    });

    this.dynamicForm = this.fb.group({});
  }

  ngAfterViewInit() {
    this.questionComponents?.forEach(
      (questionComponent: GenericDialogQuestionComponent) => {
        this.dynamicForm?.addControl(
          questionComponent.question.key,
          questionComponent.formControl
        );
      }
    );
  }

  // updateResult(k: string, e: any) {
  //   console.log(e);
  //   this.question.cb(k, e.target.value);
  // }

  onSubmit() {
    if (this.dynamicForm!.valid) {
      this.dialogRef.close(this.data);
    } else {
      const invalidControls: string[] = [];

      Object.keys(this.dynamicForm!.controls).forEach((key) => {
        const control = this.dynamicForm!.get(key);
        if (control && !control.valid) {
          invalidControls.push(this.getQuestionLabelByKey(key));
        }
      });

      if (invalidControls.length > 0) {
        const invalidControlsMessage = `The following values are not valid: ${invalidControls.join(', ')}`;
        this.messageService.snackbar(invalidControlsMessage);
      }
    }
  }

  getQuestionLabelByKey(key: string): string {
    const question = this.data.questions.find(q => q.key === key);
    return question ? question.label : key;
  }

  onCancel() {
    this.dialogRef.close(undefined);
  }

  updateMap(input: string, val: string) {
    // console.log(val);
    // console.log(this.data);
    this.data.result.set(input, val);
  }

  formatTitle(title: string): string {
    if (title == 'subIndicator') {
      return 'Sub-Indicator';
    }
    return title.charAt(0).toUpperCase() + title.substring(1, title.length);
  }
}
