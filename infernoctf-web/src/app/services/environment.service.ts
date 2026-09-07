import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface EnvironmentSettings {
  production: boolean;
  host: string;
  baseUrl: string;
  restUrl: string;
  baseEndPoint: string;
  websocketUrl: string;
}

@Injectable({
  providedIn: 'root',
})
export class EnvironmentService {
  constructor(private http: HttpClient) {}

  configUrl = 'assets/environment/app.config.json';
  private configSettings: EnvironmentSettings | undefined = undefined;

  get settings() {
    return this.configSettings;
  }

  /**
   * Loads the runtime config during app initialization. Rejects on failure rather than
   * leaving the initializer pending: every service builds its URLs from this config, so
   * there is nothing useful to do without it.
   */
  public async load(): Promise<EnvironmentSettings> {
    try {
      this.configSettings = await firstValueFrom(
        this.http.get<EnvironmentSettings>(this.configUrl));
      return this.configSettings;
    } catch (err) {
      console.error(`Could not load ${this.configUrl}; the app cannot start without it.`, err);
      throw err;
    }
  }
}
