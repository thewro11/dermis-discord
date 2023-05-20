package me.thewro.dermis.events.subscribers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.core.GatewayDiscordClient;
import me.thewro.dermis.events.ApproveButtonEventHandler;
import me.thewro.dermis.events.ApprovePaymentEventHandler;
import me.thewro.dermis.events.ApprovePaymentModalOnSubmitEventHandler;
import me.thewro.dermis.events.PayEventHandler;
import me.thewro.dermis.events.RejectButtonEventHandler;
import me.thewro.dermis.events.StatusEventHandler;
import me.thewro.dermis.events.TestCommandEventHandler;
import me.thewro.dermis.events.UpdateEventSubscriber;
import me.thewro.dermis.events.UserRegisterEventHandler;

/**
 * Add event handler and Autowired annotation.
 * Then add subscription to gateway.
 */
@Component
public class DiscordEventSubscriber {

    @Autowired
    UserRegisterEventHandler userRegisterEventHandler;

    @Autowired
    ApproveButtonEventHandler approveButtonEventHandler;

    @Autowired
    RejectButtonEventHandler rejectButtonEventHandler;

    @Autowired
    StatusEventHandler statusEventHandler;

    @Autowired
    UpdateEventSubscriber updateEventSubscriber;

    @Autowired
    TestCommandEventHandler testCommandEventHandler;

    @Autowired
    PayEventHandler payEventHandler;

    @Autowired
    ApprovePaymentEventHandler approvePaymentEventHandler;

    @Autowired
    ApprovePaymentModalOnSubmitEventHandler approvePaymentModalOnSubmitEventHandler;

    public void subscribe(GatewayDiscordClient gateway) {

        gateway.on(userRegisterEventHandler.getEventType()).subscribe(e -> {
            if (userRegisterEventHandler.triggerOn(e)) {
                userRegisterEventHandler.handle(e);
            }
        });

        gateway.on(approveButtonEventHandler.getEventType()).subscribe(e -> {
            if (approveButtonEventHandler.triggerOn(e)) {
                approveButtonEventHandler.handle(e);
            }
        });

        gateway.on(rejectButtonEventHandler.getEventType()).subscribe(e -> {
            if (rejectButtonEventHandler.triggerOn(e)) {
                rejectButtonEventHandler.handle(e);
            }
        });

        gateway.on(statusEventHandler.getEventType()).subscribe(e -> {
            if (statusEventHandler.triggerOn(e)) {
                statusEventHandler.handle(e);
            }
        });

        gateway.on(updateEventSubscriber.getEventType()).subscribe(e -> {
            if (updateEventSubscriber.triggerOn(e)) {
                updateEventSubscriber.handle(e);
            }
        });

        gateway.on(payEventHandler.getEventType()).subscribe(e -> {
            if (payEventHandler.triggerOn(e)) {
                payEventHandler.handle(e);
            }
        });

        gateway.on(approvePaymentEventHandler.getEventType()).subscribe(e -> {
            if (approvePaymentEventHandler.triggerOn(e)) {
                approvePaymentEventHandler.handle(e);
            }
        });

        gateway.on(approvePaymentModalOnSubmitEventHandler.getEventType()).subscribe(e -> {
            if (approvePaymentModalOnSubmitEventHandler.triggerOn(e)) {
                approvePaymentModalOnSubmitEventHandler.handle(e);
            }
        });

        gateway.on(testCommandEventHandler.getEventType()).subscribe(e -> {
            if (testCommandEventHandler.triggerOn(e)) {
                testCommandEventHandler.handle(e);
            }
        });

    }

}
