package me.thewro.dermis.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import me.thewro.dermis.App;
import me.thewro.dermis.config.PaymentConfig;
import me.thewro.dermis.entities.Subscriber;
import me.thewro.dermis.entities.enums.PaymentStatus;
import me.thewro.dermis.entities.repositories.PaymentRepository;
import me.thewro.dermis.entities.repositories.SubscriberRepository;

@Service
public class SubscriberService {
    
    @Autowired
    private PaymentConfig paymentConfig;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public double calculatePriceUntilNextMonth(Subscriber subscriber) {
        double monthlyPrice = paymentConfig.getMonthlyPrice();
        double balance = subscriber.getBalance();
        double amount = balance % monthlyPrice;

        if (balance < 0) {
            amount = Math.abs(balance);
        }

        return Math.abs(amount);
    }

    public double calculatePriceUntilNextYear(Subscriber subscriber) {
        LocalDateTime dueDateTime = calculateNextDueDateTime(subscriber);
        double balance = 0;
        if (!(dueDateTime.getMonthValue() == 1)) {
            int monthAmount = 13 - dueDateTime.getMonthValue();

            balance = monthAmount * paymentConfig.getMonthlyPrice();
            if (subscriber.getBalance() >= 0) {
                balance -= subscriber.getBalance() % paymentConfig.getMonthlyPrice();
            } else {
                balance -= subscriber.getBalance();
            }
        }
        return balance;
    }

    public LocalDateTime calculateNextDueDateTime(Subscriber subscriber) {
        int monthAmount = 1;
        
        double balance = subscriber.getBalance();
        double monthlyPrice = paymentConfig.getMonthlyPrice();
        LocalDateTime dueDateTime = LocalDateTime.now().withHour(19).withMinute(0).withNano(0).withSecond(0);

        if (balance >= 0) {
            monthAmount = (int)Math.floor(balance / monthlyPrice) + 1;
        }

        if (dueDateTime.getDayOfMonth() < paymentConfig.getInvoiceDate()) {
            monthAmount -= 1;
        }
        dueDateTime = dueDateTime.plusMonths(monthAmount).withDayOfMonth(paymentConfig.getInvoiceDate());

        return dueDateTime;
    }

    public void updateBalance(Subscriber subscriber, double newBalance) {
        subscriber.setBalance(newBalance);
        subscriberRepository.save(subscriber);
    }

    public void deductBalance(Subscriber subscriber) {
        updateBalance(subscriber, subscriber.getBalance() - paymentConfig.getMonthlyPrice());
    }

    @Scheduled(cron = "${payment.notification}")
    public void sendPaymentNotification() {
        List<Subscriber> subscribers = subscriberRepository.findAll();
        for (Subscriber subscriber : subscribers) {
            deductBalance(subscriber);
            if (subscriber.getBalance() < 0) {
                sendAutomaticPaymentNotification(subscriber);
            }
        }

    }

    private void sendAutomaticPaymentNotification(Subscriber subscriber) {
        User subscriberDiscordUser = App.gatewayDiscordClient.getUserById(Snowflake.of(subscriber.getUserId())).block();
        EmbedCreateSpec embedCreateSpec = EmbedCreateSpec.builder()
                                                .title("💸  Your Spotify Subscription Plan")
                                                .thumbnail(subscriberDiscordUser.getAvatarUrl())
                                                .addField("Subscriber: ", 
                                                                subscriberDiscordUser.getMention() + " (" + subscriberDiscordUser.getTag() + ")", 
                                                                false)
                                                .addField("Price (until ${month}): ", 
                                                                String.valueOf(calculatePriceUntilNextMonth(subscriber)), 
                                                                false)
                                                .addField("Price (until ${year}): ", 
                                                                String.valueOf(calculatePriceUntilNextYear(subscriber)), 
                                                                false)
                                                .build();
        PrivateChannel privateChannel = subscriberDiscordUser.getPrivateChannel().block();
        privateChannel
            .createMessage("")
            .withEmbeds(embedCreateSpec)
            .block();
    }

    public void sendPaymentNotification(Subscriber subscriber) {
        User subscriberDiscordUser = App.gatewayDiscordClient.getUserById(Snowflake.of(subscriber.getUserId())).block();
        if (subscriber.getBalance() < 0) {
            sendAutomaticPaymentNotification(subscriber);
        } else {
            EmbedCreateSpec.Builder embedCreateSpecBuilder = EmbedCreateSpec.builder()
                                                    .title("💸  Your Spotify Subscription Status")
                                                    .thumbnail(subscriberDiscordUser.getAvatarUrl())
                                                    .addField("Subscriber: ", 
                                                                    subscriberDiscordUser.getMention() + " (" + subscriberDiscordUser.getTag() + ")", 
                                                                    false);

            embedCreateSpecBuilder.addField(
                                        "Status: ",
                                        "OK",
                                        true);
            embedCreateSpecBuilder.addField(
                                    "Next Monthly Payment: ",
                                    calculateNextDueDateTime(subscriber).format(DateTimeFormatter.ofPattern("dd/MM/uuuu")),
                                    false);

            if (calculateNextDueDateTime(subscriber).isBefore(LocalDateTime.now().withYear(LocalDateTime.now().getYear() + 1).withSecond(0).withNano(0).withMonth(1).withMinute(0).withHour(19).withDayOfMonth(paymentConfig.getInvoiceDate()))) {
                embedCreateSpecBuilder.addField(
                                    "",
                                    String.format("You can wait until next month, or pay until the next year right now by **%.2f฿**.", calculatePriceUntilNextYear(subscriber)),
                                    true);
            }

            EmbedCreateSpec embedCreateSpec = embedCreateSpecBuilder.build();

            PrivateChannel privateChannel = subscriberDiscordUser.getPrivateChannel().block();
            privateChannel
                .createMessage("")
                .withEmbeds(embedCreateSpec)
                .block();
        }
    }

    public int findOnHoldPaymentAmount(Subscriber subscriber) {
        return paymentRepository.findByPayeeIdAndStatus(subscriber.getUserId(), PaymentStatus.NEW).size();
    }

}
