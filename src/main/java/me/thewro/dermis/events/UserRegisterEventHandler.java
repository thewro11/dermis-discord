package me.thewro.dermis.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import me.thewro.dermis.config.CommandConfig;

@Component
public class UserRegisterEventHandler implements DiscordEventSubscribable<ChatInputInteractionEvent>, SlashCommandable {

    @Autowired
    private CommandConfig commandConfig;

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public boolean triggerOn(ChatInputInteractionEvent event) {
        return event.getCommandId().equals(Snowflake.of(commandConfig.getRegister()));
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        System.out.println("Hello world!: handle");
    }

    @Override
    public String getCommandId() {
        return commandConfig.getRegister();
    }

}
