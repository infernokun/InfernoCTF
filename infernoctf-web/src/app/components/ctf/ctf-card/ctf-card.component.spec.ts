import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CTFCardComponent } from './ctf-card.component';

describe('CTFCardComponent', () => {
  let component: CTFCardComponent;
  let fixture: ComponentFixture<CTFCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CTFCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CTFCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
