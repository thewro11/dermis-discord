package me.thewro.dermis.events;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image.Format;
import me.thewro.dermis.App;
import me.thewro.dermis.entities.Requester;
import me.thewro.dermis.entities.Subscriber;
import me.thewro.dermis.entities.enums.ActionType;
import me.thewro.dermis.entities.enums.DeveloperCustomId;
import me.thewro.dermis.entities.repositories.RequesterRepository;
import me.thewro.dermis.services.RequesterConsiderationService;
import me.thewro.dermis.services.SubscriberService;

@Component
public class ApproveButtonEventHandler implements DiscordEventSubscribable<ButtonInteractionEvent> {

    @Autowired
    private RequesterConsiderationService requesterConsiderationService;

    @Autowired
    private SubscriberService subscriberService;

    @Autowired
    private RequesterRepository requesterRepository;

    @Override
    public Class<ButtonInteractionEvent> getEventType() {
        return ButtonInteractionEvent.class;
    }

    @Override
    public boolean triggerOn(ButtonInteractionEvent event) {
        return DeveloperCustomId.valueOf(event.getCustomId()).equals(DeveloperCustomId.BUTTON_APPROVE_REQUEST);
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        event.deferReply().withEphemeral(true).block();

        Message message = App.gatewayDiscordClient.getMessageById(App.ownerPrivateChannel.getId(), event.getInteraction().getMessageId().get()).block();
        Requester requester = requesterRepository.findByRequestMessageId(message.getId().asString());
        if (requester != null) {
            LocalDateTime currentTime = LocalDateTime.now();
            User discordUser = App.gatewayDiscordClient.getUserById(Snowflake.of(requester.getUserId())).block();
            Guild discordGuild = App.gatewayDiscordClient.getGuildById(Snowflake.of(requester.getGuildId())).block();

            EmbedCreateSpec embedCreateSpec = EmbedCreateSpec.builder()
                                                .title("✅  Approved Spotify Subscription Request")
                                                .thumbnail(discordUser.getAvatarUrl())
                                                .addField("Requester: ", 
                                                                discordUser.getMention() + " (" + discordUser.getTag() + ")", 
                                                                false)
                                                .addField("From: ", 
                                                                discordGuild.getName(), 
                                                                false)
                                                .footer(ActionType.REQUEST_APPROVED.NAME + 
                                                        " at " + 
                                                        currentTime.format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a")), 
                                                        discordGuild.getIconUrl(Format.PNG).get())
                                                .build();
            message.edit()
            .withEmbeds(embedCreateSpec)
            .withComponents()
            .block();

            Subscriber subscriber = requesterConsiderationService.approve(App.owner, requester, currentTime);

            EmbedCreateSpec embedCreateSpec2 = EmbedCreateSpec.builder()
                                                .title("✅  Your Spotify Subscription Has Been Approved!")
                                                .thumbnail(discordUser.getAvatarUrl())
                                                .addField("", "You are now a part of our Spotify family.", true)
                                                .addField("Requester: ", 
                                                                discordUser.getMention() + " (" + discordUser.getTag() + ")", 
                                                                false)
                                                .addField("From: ", 
                                                                discordGuild.getName(), 
                                                                false)
                                                .addField("Next Payment: ", 
                                                                subscriberService.calculateNextDueDateTime(subscriber).format(DateTimeFormatter.ofPattern("dd/MM/uuuu")), 
                                                                false)
                                                .footer(ActionType.REQUEST_APPROVED.NAME + 
                                                        " at " + 
                                                        currentTime.format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a")), 
                                                        discordGuild.getIconUrl(Format.PNG).get())
                                                .build();

            PrivateChannel privateChannel = discordUser.getPrivateChannel().block();
            privateChannel
                .createMessage("")
                .withEmbeds(embedCreateSpec2)
                .block();

            event.deleteReply().block();
        }
    }

}
