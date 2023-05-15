package me.thewro.dermis.events;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image.Format;
import me.thewro.dermis.App;
import me.thewro.dermis.config.CommandConfig;
import me.thewro.dermis.entities.Requester;
import me.thewro.dermis.entities.enums.ActionType;
import me.thewro.dermis.entities.enums.DeveloperCustomId;
import me.thewro.dermis.entities.repositories.RequesterRepository;

@Component
public class UserRegisterEventHandler implements DiscordEventSubscribable<ChatInputInteractionEvent> {

    @Autowired
    private CommandConfig commandConfig;

    @Autowired
    private RequesterRepository requesterRepository;

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
        String infoEphemeralMessage =
        """
        📩 **We've got your info!**
        Your request will be on consideration soon.
        We will send you a DM when your request is proceeded.
        Please do not hesitate to contact <@""" + App.owner.getId().asString() + "> for any further information.";

        try {
            event.reply(infoEphemeralMessage).withEphemeral(true).block();
            System.out.println( String.format("[Dermis] [%s] Dermis successfully responded to the request \"/%s\" (id: %s) executed by %s.", 
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a")), event.getCommandName(), event.getCommandId().asString(), event.getInteraction().getUser().getTag()));
        } catch (RuntimeException e) {
            System.err.println( String.format("[Dermis] [%s] Dermis did not respond to the request \"/%s\" (id: %s) executed by %s.", 
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a")), event.getCommandName(), event.getCommandId().asString(), event.getInteraction().getUser().getTag()));
            return;
        }

        LocalDateTime currentTime = LocalDateTime.now();
        User discordUser = event.getInteraction().getUser();
        Guild discordGuild = event.getInteraction().getGuild().block();
        EmbedCreateSpec embedCreateSpec = EmbedCreateSpec.builder()
                                            .title("👨‍💼  New Spotify Subscription Request")
                                            .thumbnail(discordUser.getAvatarUrl())
                                            .addField("Requester: ", 
                                                            discordUser.getMention() + " (" + discordUser.getTag() + ")", 
                                                            false)
                                            .addField("From: ", 
                                                            discordGuild.getName(), 
                                                            false)
                                            .footer(ActionType.REQUEST_RECEIVED.NAME + 
                                                    " at " + 
                                                    currentTime.format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a")), 
                                                    discordGuild.getIconUrl(Format.PNG).get())
                                            .build();
        Message message = App.ownerPrivateChannel
                            .createMessage("")
                            .withEmbeds(embedCreateSpec)
                            .withComponents(
                                ActionRow.of(
                                    Button.success(DeveloperCustomId.BUTTON_APPROVE.name(), ReactionEmoji.unicode("✔"), "Approve"), 
                                    Button.secondary(DeveloperCustomId.BUTTON_DENY.name(), ReactionEmoji.unicode("❌"), "Deny")
                                )
                            ).block();

        Requester requester = new Requester(discordUser.getId().asString(), discordGuild.getId().asString(), message.getId().asString(), currentTime);
        requesterRepository.save(requester);

    }

}
