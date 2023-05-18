package me.thewro.dermis.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import me.thewro.dermis.config.CommandConfig;
import me.thewro.dermis.entities.Subscriber;
import me.thewro.dermis.entities.repositories.SubscriberRepository;
import me.thewro.dermis.services.SubscriberService;

@Component
public class StatusEventHandler implements DiscordEventSubscribable<ChatInputInteractionEvent> {

    @Autowired
    private CommandConfig commandConfig;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private SubscriberService subscriberService;

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public boolean triggerOn(ChatInputInteractionEvent event) {
        return event.getCommandId().equals(Snowflake.of(commandConfig.getStatus()));
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        User discordUserSubscriber = event.getInteraction().getUser();
        Subscriber subscriber = subscriberRepository.findById(discordUserSubscriber.getId().asString()).get();
        subscriberService.sendPaymentNotification(subscriber);
    }
    
}
