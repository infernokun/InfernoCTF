import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';

import { MaterialModule } from '../../material.module';
import { UserComponent } from './user.component';

describe('UserComponent', () => {
  let component: UserComponent;
  let fixture: ComponentFixture<UserComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserComponent],
      imports: [MaterialModule, FormsModule, NoopAnimationsModule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(UserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('should create', () => {
    // ngOnInit fires the user request; consume it or verify() reports it as outstanding.
    httpMock.expectOne(req => req.url.endsWith('/user')).flush({ code: 200, message: 'ok', data: [] });
    expect(component).toBeTruthy();
  });

  it('loads the user list on init and clears the busy flag', () => {
    const request = httpMock.expectOne(req => req.url.endsWith('/user'));
    expect(component.busy()).toBeTrue();

    request.flush({ code: 200, message: 'ok', data: [{ id: '1', username: 'someone' }] });
    fixture.detectChanges();

    expect(component.busy()).toBeFalse();
    expect(component.users().length).toBe(1);
    expect(component.users()[0].username).toBe('someone');
  });
});
