import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NotificationComponent } from './shared/components/notification-component/notification.component';
import { CapacitorService } from './feature/common/service/capacitor.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NotificationComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('xpenbox-front');
  constructor(
    protected readonly capacitorService: CapacitorService
  ) {}

  async ngOnInit() {
    if (this.capacitorService.isNativePlatform()) {
      await this.capacitorService.showSplashScreen();

      setTimeout(async () => {
        await this.capacitorService.hideSplashScreen();
      }, 1000);

    }
  }
}
