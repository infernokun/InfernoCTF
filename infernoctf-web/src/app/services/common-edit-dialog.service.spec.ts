import { TestBed } from '@angular/core/testing';

import { CommonEditDialogService } from './common-edit-dialog.service';

describe('CommonEditDialogService', () => {
  let service: CommonEditDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CommonEditDialogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
