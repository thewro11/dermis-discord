package me.thewro.dermis.events;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Image.Format;
import me.thewro.dermis.App;
import me.thewro.dermis.config.CommandConfig;
import me.thewro.dermis.entities.Payment;
import me.thewro.dermis.entities.Subscriber;
import me.thewro.dermis.entities.enums.DeveloperCustomId;
import me.thewro.dermis.entities.repositories.PaymentRepository;
import me.thewro.dermis.entities.repositories.SubscriberRepository;

@Component
public class PayEventHandler implements DiscordEventSubscribable<MessageInteractionEvent> {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private CommandConfig commandConfig;

    @Override
    public Class<MessageInteractionEvent> getEventType() {
        return MessageInteractionEvent.class;
    }

    @Override
    public boolean triggerOn(MessageInteractionEvent event) {
        return event.getCommandId().equals(Snowflake.of(commandConfig.getPay()));
    }

    @Override
    public void handle(MessageInteractionEvent event) {
        event.deferReply().withEphemeral(true).block();

        User payer = event.getInteraction().getUser();
        LocalDateTime currentTime = LocalDateTime.now();
        Guild discordGuild = event.getInteraction().getGuild().block();
        List<Attachment> attachments = event.getTargetMessage().block().getAttachments();
        
        try {
            Subscriber subscriber = subscriberRepository.findById(payer.getId().asString()).get();
            for (Attachment attachment : attachments) {
                EmbedCreateSpec embedCreateSpec = EmbedCreateSpec.builder()
                    .title("👨‍💼  New Spotify Subscription Request")
                    .thumbnail(payer.getAvatarUrl())
                    .addField("Requester: ", 
                                    payer.getMention() + " (" + payer.getTag() + ")", 
                                    false)
                    .footer("Payment sent" + 
                            " at " + 
                            currentTime.format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a")), 
                            discordGuild.getIconUrl(Format.PNG).get())
                    .image(attachment.getUrl())
                    .build();
    
                Message message = App.ownerPrivateChannel.createMessage(embedCreateSpec).withComponents(
                    ActionRow.of(Button.success(DeveloperCustomId.BUTTON_APPROVE_PAYMENT.name(), "Approve"), Button.danger(DeveloperCustomId.BUTTON_DENY_PAYMENT.name(), "Deny"), Button.primary(DeveloperCustomId.BUTTON_DENY_WITHOUT_NOTICE_PAYMENT.name(), "Deny without Notice"))
                ).block();
    
                Payment payment = new Payment(message.getId().asString(), subscriber, payer.getId().asString(), payer.getId().asString(), attachment.getUrl());
                paymentRepository.save(payment);
            }
    
            event.editReply(attachments.size() + " attachment" + (attachments.size() == 1 ? " has" : "s have") + " been sent.").block();
        } catch (Exception e) {
            event.editReply("An error occurred. Please try again.").block();
        }
    
    }
    
}
