/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import moment from 'moment-timezone';

export class DateValue {
  constructor(dateString) {
    if (dateString === '') {
      this._initBlank();
    } else {
      this._init(moment(dateString));
    }
    this.editMinutes = false;
  }

  _init(initMoment) {
    this.moment = initMoment; // moment for correct timezone handling
    this.jsDate = this.moment.toDate(); // ngModel for md-date-picker
  }

  _initBlank() {
    this.moment = null;
    this.jsDate = null;
  }

  _checkInit() {
    if (this.moment === null) {
      this._init(moment());
    }
  }

  get hours() {
    return this.moment ? this.moment.hours() : null;
  }

  set hours(hours) {
    const checkedHours = (hours && hours > 23) ? 23 : hours;
    this._checkInit();
    this.moment.hours(checkedHours);
  }

  /**
   * @returns {*}
   */
  get minutes() {
    if (!this.moment) {
      return null;
    }
    const minutes = this.moment.minutes();
    if (minutes < 10 && this.editMinutes === false) {
      return `0${minutes}`; // 00 zero padding when viewing
    }
    return minutes;
  }

  set minutes(minutes) {
    this._checkInit();
    this.moment.minutes(minutes);
  }

  get date() {
    return this.jsDate;
  }

  set date(date) {
    if (date) {
      this._checkInit();
      const newMoment = moment(date);
      newMoment.hours(this.moment.hours());
      newMoment.minutes(this.moment.minutes());
      this._init(newMoment);
    } else {
      this._initBlank();
    }
  }

  toDateString() {
    return this.moment ? this.moment.format('YYYY-MM-DDTHH:mm:ss.SSSZ') : '';
  }

  setToNow() {
    this._init(moment());
  }

  focusMinutes() {
    this.editMinutes = true;
  }

  blurMinutes() {
    this.editMinutes = false;
  }
}

class DateFieldController {
  $onInit() {
    this.dateValue = new DateValue();
    this.ngModel.$render = () => {
      this.dateValue = new DateValue(this.ngModel.$viewValue);
    }
  }

  valueChanged() {
    this.ngModel.$setViewValue(this.dateValue.toDateString());
  }

  focusDateField($event = null) {
    this.onFieldFocus();
  }

  blurDateField($event = null) {
    this.onFieldBlur();
  }

  setToNow() {
    this.dateValue.setToNow();
    this.valueChanged();
  }

}

export default DateFieldController;
