import { Pipe } from "@angular/core";
import { DateService } from "../service/date.service";

@Pipe({ name: 'localDate' })
export class LocalDatePipe {
    constructor(
        private dateService: DateService
    ) { }

    transform(
        timestamp: number,
        format?: 'short' | 'long' | 'datetime'
    ): string {
        return this.dateService.format(timestamp, format);
    }
}