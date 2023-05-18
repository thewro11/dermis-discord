package me.thewro.dermis.services;

import java.time.LocalDateTime;
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
import me.thewro.dermis.entities.repositories.SubscriberRepository;

@Service
public class SubscriberService {
    
    @Autowired
    private PaymentConfig paymentConfig;

    @Autowired
    private SubscriberRepository subscriberRepository;

    public double calculatePriceUntilNextMonth(Subscriber subscriber) {
        double monthlyPrice = paymentConfig.getMonthlyPrice();
        double balance = subscriber.getBalance() + subscriber.getOnHold();
        double amount = balance % monthlyPrice;

        if (balance < 0) {
            amount = Math.abs(balance);
        }

        return Math.abs(amount);
    }

    public double calculatePriceUntilNextYear(Subscriber subscriber) {
        LocalDateTime dueDateTime = calculateNextDueDateTime(subscriber);
        int monthAmount = 1;
        if (!(dueDateTime.getMonthValue() == 1 && dueDateTime.getDayOfMonth() < paymentConfig.getInvoiceDate())) {
            monthAmount = 13 - dueDateTime.getMonthValue();
        }
        double balance = subscriber.getBalance() - monthAmount * paymentConfig.getMonthlyPrice();

        return Math.abs(balance);
    }

    public LocalDateTime calculateNextDueDateTime(Subscriber subscriber) {
        int monthAmount = 1;
        
        double balance = subscriber.getBalance() + subscriber.getOnHold();
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
        } else {
            EmbedCreateSpec.Builder embedCreateSpecBuilder = EmbedCreateSpec.builder()
                                                    .title("💸  Your Spotify Subscription Plan")
                                                    .thumbnail(subscriberDiscordUser.getAvatarUrl())
                                                    .addField("Subscriber: ", 
                                                                    subscriberDiscordUser.getMention() + " (" + subscriberDiscordUser.getTag() + ")", 
                                                                    false);
            int monthAmount = (int)(subscriber.getBalance() / paymentConfig.getMonthlyPrice());
            int remainders = (int)(subscriber.getBalance() % paymentConfig.getMonthlyPrice());

            if (monthAmount == 0) {
                embedCreateSpecBuilder.addField(
                                            "", 
                                           "You have already paid for this month" + ((remainders == 0) ? "." : String.format(" with %.2f remainders.", remainders)),
                                          false);
            } else {
                embedCreateSpecBuilder.addField(
                                            "", 
                                           "You have already paid for the next " + String.format(" %s month" + (monthAmount == 1 ? "" : "s"), monthAmount - 1) + ((remainders == 0) ? "." : String.format(" with %.2f remainders.", remainders)),
                                          false);
            }

            EmbedCreateSpec embedCreateSpec = embedCreateSpecBuilder.build();

            PrivateChannel privateChannel = subscriberDiscordUser.getPrivateChannel().block();
            privateChannel
                .createMessage("")
                .withEmbeds(embedCreateSpec)
                .block();
        }
    }

}
