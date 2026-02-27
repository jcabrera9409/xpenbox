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

  /**
   * Converts an ISO date string (YYYY-MM-DD) selected by the user in their local timezone 
   * to a UTC timestamp. The date is interpreted as being in the client's local timezone,
   * then converted to UTC.
   * 
   * Example: User in UTC-5 selects "2026-01-27" at 00:00 local time
   *          -> Returns timestamp for "2026-01-27T05:00:00.000Z"
   * 
   * @param dateIsoString The date string in ISO format (YYYY-MM-DD) from a date input
   * @param hours The hour in LOCAL timezone (0-23)
   * @param minutes The minute (0-59)
   * @param seconds The second (0-59)
   * @param milliseconds The millisecond (0-999)
   * @returns The UTC timestamp in milliseconds
   */
  parseDateIsoStringToUtcTimestamp(
    dateIsoString: string
  ): Date {
    const now = new Date();
    const [year, month, day] = dateIsoString.split('-').map(num => parseInt(num, 10));
    // Create date in LOCAL timezone, JavaScript automatically handles UTC conversion
    const localDate = new Date(year, month - 1, day, now.getHours(), now.getMinutes(), now.getSeconds(), now.getMilliseconds());
    return localDate;
  }

  /**
   * Formats a Date object to an ISO date string (YYYY-MM-DD) in the client's local timezone.
   * @param date The Date object to format
   * @returns The formatted date string in ISO format (YYYY-MM-DD)
   */
  formatLocalDateToIso(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  /**
   * Converts a UTC timestamp to an ISO date string (YYYY-MM-DD) in the client's local timezone.
   * @param timestamp The UTC timestamp in milliseconds
   * @returns The formatted date string in ISO format (YYYY-MM-DD) representing the local date
   */
  formatUtcTimestampToLocalIso(timestamp: number): string {
    const date = new Date(timestamp);
    return this.formatLocalDateToIso(date);
  }

  addDays(date: Date, days: number): Date {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
  }

  format(
    timestampUtc: number,
    format:  'day-month' | 'short' | 'long' | 'date' | 'datetime' | 'ISO' | 'ISO-LOCAL' = 'datetime'
  ): string {
    const date = this.toDate(timestampUtc);

    const options: Intl.DateTimeFormatOptions = {};

    switch (format) {
      case 'day-month':
        options.day = '2-digit';
        options.month = 'short';
        break;
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
