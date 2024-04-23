import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CtfMainComponent } from './ctf-main.component';

describe('CtfMainComponent', () => {
  let component: CtfMainComponent;
  let fixture: ComponentFixture<CtfMainComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CtfMainComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CtfMainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
