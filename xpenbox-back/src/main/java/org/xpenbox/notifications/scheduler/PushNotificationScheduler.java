package org.xpenbox.notifications.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.DateFunctions;
import org.xpenbox.creditcard.entity.CreditCard;
import org.xpenbox.creditcard.repository.CreditCardRepository;
import org.xpenbox.notifications.entity.DeviceToken;
import org.xpenbox.notifications.entity.DeviceToken.Platform;
import org.xpenbox.notifications.repository.DeviceTokenRepository;
import org.xpenbox.notifications.service.IPushNotificationService;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

/**
 * Scheduler class for managing push notification-related tasks. This class is responsible for scheduling and executing tasks related to push notifications, such as sending notifications to users at specified intervals. It uses the Quarkus Scheduler to run tasks at specified intervals, allowing for automated management of push notification delivery based on defined schedules.
 */
@Singleton
public class PushNotificationScheduler {
    private static final Logger LOG = Logger.getLogger(PushNotificationScheduler.class.getName());

    private final DeviceTokenRepository deviceTokenRepository;
    private final IPushNotificationService pushNotificationService;
    private final UserRepository userRepository;
    private final CreditCardRepository creditCardRepository;


    public PushNotificationScheduler(
        IPushNotificationService pushNotificationService,
        DeviceTokenRepository deviceTokenRepository,
        UserRepository userRepository,
        CreditCardRepository creditCardRepository
    ) {
        this.pushNotificationService = pushNotificationService;
        this.deviceTokenRepository = deviceTokenRepository;
        this.userRepository = userRepository;
        this.creditCardRepository = creditCardRepository;
    }

    @Scheduled(cron = "{firebase.push.notification.activity.cron}")
    @Transactional
    void schedulePushNotificationActivityTask() {
        LOG.info("Running scheduled push notification activity task");

        List<User> usersWithoutTransactions = findUsersWithoutTransactions();
        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByStateTrueAndPlatform(Platform.ANDROID);

        LOG.infof("Found %d users without transactions", usersWithoutTransactions.size());
        
        for (User user : usersWithoutTransactions) {
            LOG.debugf("User without transactions: %s (ID: %d)", user.getEmail(), user.id);
            
            DeviceToken deviceToken = deviceTokens.stream()
                .filter(dt -> dt.getUser().id.equals(user.id))
                .findFirst()
                .orElse(null);
            
            if (deviceToken != null) {
                pushNotificationService.sendPushNotification(
                    deviceToken.getToken(),
                    "Un minuto para tus finanzas",
                    "Registrar tus gastos diariamente te ayuda a tener un mejor control de tu dinero."
                );
            } else {
                LOG.debugf("No active Android device token found for user: %s (ID: %d)", user.getEmail(), user.id);
            }
        }

    }

    @Scheduled(cron = "{firebase.push.notification.creditcard.expiration.cron}")
    @Transactional
    public void schedulePushNotificationCreditCardExpirationTask() {
        LOG.info("Running scheduled push notification credit card expiration task");
        LocalDateTime now = DateFunctions.currentLocalDateTime();
        byte currentDay = (byte) now.getDayOfMonth();
        byte nextDay = (byte) now.plusDays(1).getDayOfMonth();

        List<CreditCard> creditCards = findActiveCreditCardsNotification(currentDay, nextDay);
        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByStateTrueAndPlatform(Platform.ANDROID);

        for (CreditCard creditCard : creditCards) {
            LOG.debugf("Active credit card: %s (ID: %d)", creditCard.getName(), creditCard.id);
            List<DeviceToken> userDeviceTokens = deviceTokens.stream()
                .filter(dt -> dt.getUser().id.equals(creditCard.getUser().id))
                .toList();
            for (DeviceToken deviceToken : userDeviceTokens) {
                LOG.debugf("User device token: %s (ID: %d)", deviceToken.getToken(), deviceToken.id);
                String creditCardName = creditCard.getName();
                if (creditCard.getPaymentDay() == currentDay) {
                    pushNotificationService.sendPushNotification(
                        deviceToken.getToken(),
                        "Tu " + creditCardName + " vence hoy 💳",
                        "Realiza tu pago a tiempo para evitar intereses y cargos adicionales."
                    );
                } else if(creditCard.getPaymentDay() == nextDay) {
                    pushNotificationService.sendPushNotification(
                        deviceToken.getToken(),
                        "Tu " + creditCardName + " vence mañana 💳",
                        "Recuerda pagar tu " + creditCardName + " antes de la fecha límite para evitar intereses."
                    );
                } else if(creditCard.getBillingDay() == nextDay) {
                    pushNotificationService.sendPushNotification(
                        deviceToken.getToken(),
                        "Tu " + creditCardName + " tiene fecha de corte mañana 🗓️",
                        "Revisa tus gastos y prepárate para tu fecha de corte."
                    );
                }
            } 
        }
    }

    private List<User> findUsersWithoutTransactions() {
        LOG.info("Finding all users without transactions");
        LocalDateTime now = DateFunctions.currentLocalDateTime();
        LocalDateTime startDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endDay = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return userRepository.findAllUsersWithoutTransactionsAndDates(startDay, endDay);
    }

    private List<CreditCard> findActiveCreditCardsNotification(byte currentDay, byte nextDay) {
        LOG.infof("Finding all active credit cards with billing day in [%d, %d] or payment day in [%d, %d]", currentDay, nextDay, currentDay, nextDay);
        return creditCardRepository.findAllActiveByBillingDayOrPaymentDay(
            List.of(currentDay, nextDay), 
            List.of(currentDay, nextDay)
        );
    }
    
}
