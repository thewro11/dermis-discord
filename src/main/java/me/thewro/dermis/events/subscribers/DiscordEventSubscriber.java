package me.thewro.dermis.events.subscribers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.core.GatewayDiscordClient;
import me.thewro.dermis.events.ApproveButtonEventHandler;
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

    }

}
