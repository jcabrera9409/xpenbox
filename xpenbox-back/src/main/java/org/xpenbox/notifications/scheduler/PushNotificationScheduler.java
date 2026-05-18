package org.xpenbox.notifications.scheduler;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.xpenbox.common.DateFunctions;
import org.xpenbox.creditcard.entity.CreditCard;
import org.xpenbox.creditcard.repository.CreditCardRepository;
import org.xpenbox.notifications.entity.DeviceToken;
import org.xpenbox.notifications.entity.DeviceToken.Platform;
import org.xpenbox.notifications.repository.DeviceTokenRepository;
import org.xpenbox.notifications.service.IPushNotificationService;
import org.xpenbox.transaction.entity.Transaction;
import org.xpenbox.transaction.repository.TransactionRepository;
import org.xpenbox.user.entity.User;

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
    private final TransactionRepository transactionRepository;
    private final CreditCardRepository creditCardRepository;


    public PushNotificationScheduler(
        IPushNotificationService pushNotificationService,
        DeviceTokenRepository deviceTokenRepository,
        TransactionRepository transactionRepository,
        CreditCardRepository creditCardRepository
    ) {
        this.pushNotificationService = pushNotificationService;
        this.deviceTokenRepository = deviceTokenRepository;
        this.transactionRepository = transactionRepository;
        this.creditCardRepository = creditCardRepository;
    }

    @Scheduled(cron = "{firebase.push.notification.activity.cron}")
    @Transactional
    void schedulePushNotificationActivityTask() {
        LOG.info("Running scheduled push notification activity task");

        LocalDateTime now = DateFunctions.currentLocalDateTime();
        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByStateTrueAndPlatform(Platform.ANDROID);
        List<User> users = filterUsersFromDeviceList(deviceTokens);
        List<Transaction> transactions = findUserTransactions();
        Map<Long, Transaction> lastTransactionByUser = transactions.stream()
            .collect(Collectors.toMap(
                t -> t.getUser().id,
                Function.identity()
            ));
        Map<Long, List<DeviceToken>> deviceTokensByUser = deviceTokens.stream()
            .collect(Collectors.groupingBy(dt -> dt.getUser().id));

        LOG.infof("Found %d users with active device tokens and %d transactions in the last 14 days", users.size(), transactions.size());
        
        for (User user : users) {
            LOG.debugf("User without transactions: %s (ID: %d)", user.getEmail(), user.id);
            Transaction lastTransaction = lastTransactionByUser.get(user.id);
            
            // substract between now and last transaction date
            long daysWithoutTransactions = lastTransaction != null ? DateFunctions.daysBetween(lastTransaction.getTransactionDate(), now) : Long.MAX_VALUE;
            
            if (daysWithoutTransactions < 3 || lastTransaction == null) {
                LOG.debugf("User %s has made a transaction %d days ago, skipping notification", user.getEmail(), daysWithoutTransactions);
                continue;
            }

            String title = "";
            String message = "";

            if (daysWithoutTransactions == 14) {
                title = "Retoma el control 🚀";
                message = "Volver a registrar tus movimientos solo toma unos minutos.";
            } else if (daysWithoutTransactions == 7) {
                title = "Tus finanzas te necesitan 📊";
                message = "Registrar tus gastos con frecuencia te ayuda a evitar sorpresas a fin de mes.";
            } else if (daysWithoutTransactions == 3) {
                title = "No pierdas el control 💰";
                message = "Han pasado unos días desde tu último registro. Mantén tus movimientos al día.";
            }

            List<DeviceToken> userDeviceTokens = deviceTokensByUser.getOrDefault(user.id, Collections.emptyList());

            LOG.debugf("Found %d device tokens for user %s", userDeviceTokens.size(), user.getEmail());

            for (DeviceToken deviceToken : userDeviceTokens) {
                pushNotificationService.sendPushNotification(
                    deviceToken.getToken(),
                    title,
                    message
                );
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
        Map<Long, List<DeviceToken>> deviceTokensByUser = deviceTokens.stream()
            .collect(Collectors.groupingBy(dt -> dt.getUser().id));

        for (CreditCard creditCard : creditCards) {
            LOG.debugf("Active credit card: %s (ID: %d)", creditCard.getName(), creditCard.id);
            List<DeviceToken> userDeviceTokens = deviceTokensByUser.getOrDefault(creditCard.getUser().id, Collections.emptyList());

            for (DeviceToken deviceToken : userDeviceTokens) {
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

    private List<User> filterUsersFromDeviceList(List<DeviceToken> deviceTokens) {
        return deviceTokens.stream()
            .map(DeviceToken::getUser)
            .distinct()
            .toList();
    }

    private List<Transaction> findUserTransactions() {
        LOG.info("Finding all users without transactions");
        LocalDateTime now = DateFunctions.currentLocalDateTime();
        LocalDateTime startDate = now.minusDays(20);

        return transactionRepository.findLastTransactionForUsersBetweenDates(startDate, now);
    }

    private List<CreditCard> findActiveCreditCardsNotification(byte currentDay, byte nextDay) {
        LOG.infof("Finding all active credit cards with billing day in [%d, %d] or payment day in [%d, %d]", currentDay, nextDay, currentDay, nextDay);
        return creditCardRepository.findAllActiveByBillingDayOrPaymentDay(
            List.of(currentDay, nextDay), 
            List.of(currentDay, nextDay)
        );
    }
    
}
