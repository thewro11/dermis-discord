package me.thewro.dermis.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.thewro.dermis.config.PaymentConfig;
import me.thewro.dermis.entities.Subscriber;

@Service
public class SubscriberService {
    
    @Autowired
    private PaymentConfig paymentConfig;

    public double calculateNextDueMonthPrice(Subscriber subscriber) {
        double amount;

        double balance = subscriber.getBalance();
        double monthlyPrice = paymentConfig.getMonthlyPrice();
        if (balance < 0) {
            amount = Math.abs(balance) + monthlyPrice;
        } else {
            amount = monthlyPrice - balance % monthlyPrice;
        }

        return amount;
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

    public void deductBalance(Subscriber subscriber, double deductingBalance) {
        subscriber.setBalance(subscriber.getBalance() - deductingBalance);
    }

    public void deductBalance(Subscriber subscriber) {
        deductBalance(subscriber, paymentConfig.getMonthlyPrice());
    }

}
