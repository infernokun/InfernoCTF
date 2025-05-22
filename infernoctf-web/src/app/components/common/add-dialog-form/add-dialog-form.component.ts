import {
  AfterViewInit,
  Component,
  Inject,
  OnInit,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { DialogQuestionComponent } from '../dialog-question/dialog-question.component';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { SimpleFormData } from '../../../models/SimpleFormData.model';
import { MessageService } from '../../../services/message.service';

@Component({
  selector: 'app-add-dialog-form',
  templateUrl: './add-dialog-form.component.html',
  styleUrls: ['./add-dialog-form.component.scss'],
  standalone: false
})
export class AddDialogFormComponent implements OnInit, AfterViewInit {
  @ViewChildren(DialogQuestionComponent) questionComponents: QueryList<DialogQuestionComponent> | undefined;

  dynamicForm: FormGroup | undefined;

  isLoading = true;
  isSubmitting = false;

  constructor(
    public dialogRef: MatDialogRef<AddDialogFormComponent>,
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
      (questionComponent: DialogQuestionComponent) => {
        this.dynamicForm?.addControl(
          questionComponent.question.key,
          questionComponent.formControl
        );
      }
    );

    this.isLoading = false;
  }

  get hasQuestions(): boolean {
    return this.data?.questions?.length > 0;
  }

  get canSubmit(): boolean {
    return this.dynamicForm?.valid! && !this.isSubmitting;
  }

  onSubmit() {
    if (this.dynamicForm!.valid) {
      this.dialogRef.close(this.data);
      this.isSubmitting = true;
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
      this.isSubmitting = false;
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
    return title.charAt(0).toUpperCase() + title.substring(1, title.length);
  }


  formatCamelCase(text: string): string {
    if (!text) return '';

    const spaced = text.replace(/([A-Z])/g, ' $1');

    return spaced.charAt(0).toUpperCase() + spaced.slice(1).trim();
  }
}
