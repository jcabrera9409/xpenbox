import { Injectable, signal } from '@angular/core';
import { NotificationDTO, NotificationType } from '../model/notification.dto';

/**
 * Service for managing notifications within the application.
 * Provides methods to create, dismiss, and clear notifications.
 * Notifications are automatically dismissed after a set duration.
 * Limits the number of concurrent notifications to avoid overwhelming the user.
 * Uses Angular signals for reactive state management.
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly MAX_NOTIFICATIONS = 3;
  private readonly AUTO_DISMISS_TIME = 5000;

  private notificationsSignal = signal<NotificationDTO[]>([]);
  
  readonly notifications = this.notificationsSignal.asReadonly();

  private generateId(): string {
    return `notification-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  private addNotification(type: NotificationType, message: string): void {
    const notification: NotificationDTO = {
      id: this.generateId(),
      type,
      message,
      timestamp: Date.now()
    };

    this.notificationsSignal.update(notifications => {
      const updated = [...notifications, notification];
      if (updated.length > this.MAX_NOTIFICATIONS) {
        return updated.slice(updated.length - this.MAX_NOTIFICATIONS);
      }
      return updated;
    });

    setTimeout(() => {
      this.dismiss(notification.id);
    }, this.AUTO_DISMISS_TIME);
  }

  // Convenience methods for different notification types

  // Info notification
  info(message: string): void {
    this.addNotification('info', message);
  }

  // Success notification
  success(message: string): void {
    this.addNotification('success', message);
  }

  // Warning notification
  warning(message: string): void {
    this.addNotification('warning', message);
  }

  // Error notification
  error(message: string): void {
    this.addNotification('error', message);
  }

  // Dismiss a notification by ID
  dismiss(id: string): void {
    this.notificationsSignal.update(notifications =>
      notifications.filter(n => n.id !== id)
    );
  }

  // Clear all notifications
  clearAll(): void {
    this.notificationsSignal.set([]);
  }
}
