package me.thewro.dermis.events;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import me.thewro.dermis.App;
import me.thewro.dermis.entities.Payment;
import me.thewro.dermis.entities.enums.DeveloperCustomId;
import me.thewro.dermis.entities.enums.PaymentStatus;
import me.thewro.dermis.entities.repositories.PaymentRepository;

@Component
public class SilentlyRejectPaymentButtonEventHandler implements DiscordEventSubscribable<ButtonInteractionEvent> {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Class<ButtonInteractionEvent> getEventType() {
        return ButtonInteractionEvent.class;
    }

    @Override
    public boolean triggerOn(ButtonInteractionEvent event) {
        return DeveloperCustomId.valueOf(event.getCustomId()).equals(DeveloperCustomId.BUTTON_DENY_WITHOUT_NOTICE_PAYMENT); 
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        event.deferReply().block();

        LocalDateTime currentTime = LocalDateTime.now();
        String messageId = event.getInteraction().getMessageId().get().asString();

        Payment payment = paymentRepository.findById(messageId).get();
        payment.setStatus(PaymentStatus.DENIED);

        
        paymentRepository.save(payment);

        User discordUserSubscriber = App.gatewayDiscordClient.getUserById(Snowflake.of(payment.getPayeeId())).block();

        EmbedCreateSpec embedCreateSpec2 = EmbedCreateSpec.builder()
            .title("👨‍💼  New Spotify Subscription Request")
            .thumbnail(discordUserSubscriber.getAvatarUrl())
            .addField("Requester: ", 
                            discordUserSubscriber.getMention() + " (" + discordUserSubscriber.getTag() + ")", 
                            false)
            .footer("Payment verification failed silently" + 
                    " at " + 
                    currentTime.format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a")), 
                    App.owner.getAvatarUrl())
            .image(payment.getPaymentUrl())
            .build();

        Message message = App.gatewayDiscordClient.getMessageById(App.ownerPrivateChannel.getId(), Snowflake.of(payment.getMessageId())).block();

        message.edit()
        .withEmbeds(embedCreateSpec2)
        .withComponents()
        .block();

        event.deleteReply().block();
    }
    
}
