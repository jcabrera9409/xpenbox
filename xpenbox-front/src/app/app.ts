import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NotificationComponent } from './shared/components/notification-component/notification.component';
import { CapacitorService } from './feature/common/service/capacitor.service';
import { AppStateService } from './feature/common/service/app-state.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NotificationComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  protected readonly title = signal('xpenbox-front');

  constructor(
    protected readonly capacitorService: CapacitorService,
    private appStateService: AppStateService
  ) {}

  async ngOnInit() {
    if (this.capacitorService.isNativePlatform()) {
      await this.capacitorService.showSplashScreen();

      await this.appStateService.initialize();

      setTimeout(async () => {
        await this.capacitorService.hideSplashScreen();
      }, 1000);
    }
  }

  async ngOnDestroy() {
    if (this.capacitorService.isNativePlatform()) {
      await this.appStateService.cleanup();
    }
  }
}
