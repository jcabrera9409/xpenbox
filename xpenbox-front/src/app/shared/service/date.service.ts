import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class DateService {

  toDate(timestampUtc: number): Date {
    return new Date(timestampUtc); 
  }

  toTimestamp(date: Date): number {
    return date.getTime(); 
  }

  toLocalDate(timestampUtc: number): Date {
    const date = new Date(timestampUtc);
    const timezoneOffset = date.getTimezoneOffset();
    return new Date(date.getTime() - timezoneOffset * 60000);
  }
  
  toUtcDate(date: Date): Date {
    const timezoneOffset = date.getTimezoneOffset();
    return new Date(date.getTime() + timezoneOffset * 60000);
  }

  getUtcDatetime(): Date {
    return new Date();
  }

  getLocalDatetime(): Date {
    const now = new Date();
    const timezoneOffset = now.getTimezoneOffset();
    return new Date(now.getTime() - timezoneOffset * 60000);
  }

  parseDateIsoString(datetimeString: string): Date {
    const [year, month, day] = datetimeString.split('-').map(num => parseInt(num, 10));
    return new Date(year, month - 1, day);
  }
  parseDatetimeIsoString(datetimeString: string): Date {
    const [datePart, timePart] = datetimeString.split('T');
    const [year, month, day] = datePart.split('-').map(num => parseInt(num, 10));
    const [hours, minutes] = timePart.split(':').map(num => parseInt(num, 10));
    return new Date(year, month - 1, day, hours, minutes);
  }

  format(
    timestampUtc: number,
    format: 'short' | 'long' | 'date' | 'datetime' | 'ISO' | 'ISO-LOCAL' = 'datetime'
  ): string {
    const date = this.toDate(timestampUtc);

    const options: Intl.DateTimeFormatOptions = {};

    switch (format) {
      case 'short':
        options.day = '2-digit';
        options.month = 'short';
        options.year = 'numeric';
        break;
      case 'long':
        options.day = '2-digit';
        options.month = 'long';
        options.year = 'numeric';
        break;
      case 'date':
        options.day = '2-digit';
        options.month = '2-digit';
        options.year = 'numeric';
        break;
      case 'datetime':
        options.day = '2-digit';
        options.month = '2-digit';
        options.year = 'numeric';
        options.hour = '2-digit';
        options.minute = '2-digit';
        options.hour12 = false;
        break;
      case 'ISO':
        return date.toISOString();
      case 'ISO-LOCAL':
        return date.toISOString().slice(0, 16);
    }

    return date.toLocaleDateString('es-PE', options);
  }
}
