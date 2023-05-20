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
import me.thewro.dermis.entities.enums.ActionType;
import me.thewro.dermis.entities.enums.DeveloperCustomId;
import me.thewro.dermis.entities.repositories.RequesterRepository;
import me.thewro.dermis.services.RequesterConsiderationService;

@Component
public class RejectButtonEventHandler implements DiscordEventSubscribable<ButtonInteractionEvent> {

    @Autowired
    private RequesterRepository requesterRepository;

    @Autowired
    private RequesterConsiderationService requesterConsiderationService;

    @Override
    public Class<ButtonInteractionEvent> getEventType() {
        return ButtonInteractionEvent.class;
    }

    @Override
    public boolean triggerOn(ButtonInteractionEvent event) {
        return DeveloperCustomId.valueOf(event.getCustomId()).equals(DeveloperCustomId.BUTTON_DENY_REQUEST);
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
                                                .title("⛔️  Rejected Spotify Subscription Request")
                                                .thumbnail(discordUser.getAvatarUrl())
                                                .addField("Requester: ", 
                                                                discordUser.getMention() + " (" + discordUser.getTag() + ")", 
                                                                false)
                                                .addField("From: ", 
                                                                discordGuild.getName(), 
                                                                false)
                                                .footer(ActionType.REQUEST_DENIED.NAME + 
                                                        " at " + 
                                                        currentTime.format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a")), 
                                                        discordGuild.getIconUrl(Format.PNG).get())
                                                .build();
            message.edit()
            .withEmbeds(embedCreateSpec)
            .withComponents()
            .block();

            requesterConsiderationService.reject(App.owner, requester, currentTime);

            EmbedCreateSpec embedCreateSpec2 = EmbedCreateSpec.builder()
                                                .title("⛔️  Your Spotify Subscription Has Been Rejected")
                                                .thumbnail(discordUser.getAvatarUrl())
                                                .addField("", "We regret to inform you that you are not qualified for this subscription.", true)
                                                .addField("Requester: ", 
                                                                discordUser.getMention() + " (" + discordUser.getTag() + ")", 
                                                                false)
                                                .addField("From: ", 
                                                                discordGuild.getName(), 
                                                                false)
                                                .footer(ActionType.REQUEST_DENIED.NAME + 
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
