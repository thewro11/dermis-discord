package me.thewro.dermis.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import me.thewro.dermis.config.CommandConfig;
import me.thewro.dermis.entities.repositories.SubscriberRepository;
import me.thewro.dermis.services.SubscriberService;

@Component
public class TestCommandEventHandler implements DiscordEventSubscribable<ChatInputInteractionEvent> {

    @Autowired
    private SubscriberService subscriberService;

    @Autowired
    private CommandConfig commandConfig;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public boolean triggerOn(ChatInputInteractionEvent event) {
        return event.getCommandId().equals(Snowflake.of(commandConfig.getTest()));
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        subscriberService.sendPaymentNotification(subscriberRepository.findById("157475157336653824").get());
    }
    
}
