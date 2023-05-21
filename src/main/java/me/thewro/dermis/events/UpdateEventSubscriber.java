package me.thewro.dermis.events;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import me.thewro.dermis.config.CommandConfig;
import me.thewro.dermis.entities.Subscriber;
import me.thewro.dermis.entities.repositories.SubscriberRepository;

@Component
public class UpdateEventSubscriber implements DiscordEventSubscribable<ChatInputInteractionEvent> {

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
        return event.getCommandId().equals(Snowflake.of(commandConfig.getUpdate()));
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        try {
            event.deferReply().withEphemeral(true).block();
        } catch (Exception e) {
            
        }

        List<ApplicationCommandInteractionOption> options = event.getOptions();
        User discordUserSubscriber = options.get(0).getValue().get().asUser().block();
        double balance = options.get(1).getValue().get().asDouble();
        String reason = "";
        if (options.size() > 2) {
            reason = options.get(2).getValue().get().asString();
        }

        try {
            Subscriber subscriber = subscriberRepository.findById(discordUserSubscriber.getId().asString()).get();
            subscriber.setBalance(balance);
            subscriberRepository.save(subscriber);
    
            EmbedCreateSpec embedCreateSpec = EmbedCreateSpec.builder()
                                                    .title("💸  Your Spotify payment has been updated.")
                                                    .thumbnail(discordUserSubscriber.getAvatarUrl())
                                                    .addField("Subscriber: ", 
                                                                    discordUserSubscriber.getMention() + " (" + discordUserSubscriber.getTag() + ")", 
                                                                    false)
                                                    .addField("Balance: ", 
                                                                    String.valueOf(subscriber.getBalance()),
                                                                    false)
                                                    .addField("Reason: ", 
                                                                    reason,
                                                                    false)
                                                    .build();
            PrivateChannel privateChannel = discordUserSubscriber.getPrivateChannel().block();
            privateChannel
                .createMessage("")
                .withEmbeds(embedCreateSpec)
                .block();
    
            event.deleteReply().block();
            
        } catch (Exception e) {
            event.editReply("An error occurred. Please try again.").block();
        }
    }
    
}
