import {Injectable, OnInit} from "@angular/core";
import {UserApi} from "../api/user-api";
import {UserPreference} from "../dto/user-preference";
import {MatCheckboxChange} from "@angular/material/checkbox";
import {MatSelectChange} from "@angular/material/select";
import {MatRadioChange} from "@angular/material/radio";
import {MatDatepickerInputEvent} from "@angular/material/datepicker";

@Injectable({
  providedIn: 'root'
})
export class UserPreferenceService implements OnInit {
  static preferences: any = {};
  static userApi: UserApi;

  constructor(private userApi: UserApi) {
    UserPreferenceService.userApi = userApi;
    this.userApi.getPreferences().subscribe(preferences => {
      UserPreferenceService.preferences = preferences;
    });
  }

  ngOnInit(): void {
  }

  static get<T>(key: string, defaultValue: T): T {
    if (UserPreferenceService.preferences[key] == null) {
      UserPreferenceService.set(key, defaultValue as T);
    }
    return UserPreferenceService.preferences[key];
  }

  static set<T>(key: string, value: T): void {
    this.preferences[key] = value;
    UserPreferenceService.userApi.setPreference(key, value as string).subscribe();
  }

  static onCheckboxChange(key: UserPreference, checked: boolean) {
    UserPreferenceService.set<boolean>(key.valueOf(), checked);
  }

  static onSelectChange(key: UserPreference, values: any) {
    if(Array.isArray(values)) {
      UserPreferenceService.set<string>(key.valueOf(), values.join(','));
    } else {
      UserPreferenceService.set<string>(key.valueOf(), values);
    }
  }

  static onRadioChange(key: UserPreference, value: string) {
    UserPreferenceService.set(key.valueOf(), value);
  }

  static onDatePickerChange(key: UserPreference, value: any) {
    UserPreferenceService.set<string>(key.valueOf(), value);
  }

  static onInputChange(key: UserPreference, event: any) {
    UserPreferenceService.set<string>(key.valueOf(), event.target.value);
  }
}
