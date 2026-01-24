/**
 * Data Transfer Object representing a notification.
 * Includes the notification's unique ID, type, message, and timestamp.
 */
export interface NotificationDTO {
  id: string;
  type: NotificationType;
  message: string;
  timestamp: number;
}

// Possible types of notifications.
export type NotificationType = 'info' | 'success' | 'warning' | 'error';