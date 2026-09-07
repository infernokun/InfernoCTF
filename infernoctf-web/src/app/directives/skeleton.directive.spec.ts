import { TemplateRef, ViewContainerRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { SkeletonDirective } from './skeleton.directive';

describe('SkeletonDirective', () => {
  it('should create an instance', () => {
    // The directive takes TemplateRef/ViewContainerRef, so it cannot be `new`ed
    // bare - let the injector supply them.
    TestBed.configureTestingModule({
      providers: [
        SkeletonDirective,
        { provide: TemplateRef, useValue: {} },
        { provide: ViewContainerRef, useValue: { clear: () => {}, createComponent: () => {} } },
      ],
    });

    expect(TestBed.inject(SkeletonDirective)).toBeTruthy();
  });
});
