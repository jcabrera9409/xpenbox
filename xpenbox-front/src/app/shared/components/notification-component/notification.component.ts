import { Component, ChangeDetectionStrategy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../feature/common/service/notification.service';
import { NotificationDTO, NotificationType } from '../../../feature/common/model/notification.dto';

/**
 * Component to display notifications to the user.
 * Subscribes to the NotificationService to get the current list of notifications.
 * Provides methods to dismiss individual notifications.
 * Uses Angular's ChangeDetectionStrategy.OnPush for performance optimization.
 */
@Component({
  selector: 'app-notification-component',
  imports: [CommonModule],
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotificationComponent {
  notifications = computed(() => this.notificationService.notifications());

  constructor(private notificationService: NotificationService) {}

  /**
   * Returns the icon name based on the notification type.
   * @param type The type of the notification.
   * @returns The icon name as a string.
   */
  getNotificationIcon(type: NotificationType): string {
    const icons: Record<NotificationType, string> = {
      info: 'info',
      success: 'check_circle',
      warning: 'warning',
      error: 'error'
    };
    return icons[type];
  }

  /**
   * Returns the CSS classes for the notification based on its type.
   * @param type The type of the notification.
   * @returns The CSS classes as a string.
   */
  getNotificationStyles(type: NotificationType): string {
    const styles: Record<NotificationType, string> = {
      info: 'bg-blue-50 border-blue-200 text-blue-800',
      success: 'bg-green-50 border-green-200 text-green-800',
      warning: 'bg-yellow-50 border-yellow-200 text-yellow-800',
      error: 'bg-red-50 border-red-200 text-red-800'
    };
    return styles[type];
  }

  /**
   * Returns the icon color class based on the notification type.
   * @param type The type of the notification.
   * @returns The icon color class as a string.
   */
  getIconColor(type: NotificationType): string {
    const colors: Record<NotificationType, string> = {
      info: 'text-blue-500',
      success: 'text-[var(--xpb-income)]',
      warning: 'text-yellow-500',
      error: 'text-[var(--xpb-expense)]'
    };
    return colors[type];
  }

  /**
   * Dismisses a notification by its ID.
   * @param id The ID of the notification to dismiss.
   */
  dismiss(id: string): void {
    this.notificationService.dismiss(id);
  }

  /**
   * TrackBy function for notifications to optimize ngFor rendering.
   * @param index The index of the notification in the list.
   * @param item The notification item.
   * @returns The unique ID of the notification.
   */
  trackById(index: number, item: NotificationDTO): string {
    return item.id;
  }
}
