package me.thewro.dermis.events;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import me.thewro.dermis.App;
import me.thewro.dermis.entities.Payment;
import me.thewro.dermis.entities.Subscriber;
import me.thewro.dermis.entities.enums.DeveloperCustomId;
import me.thewro.dermis.entities.enums.PaymentStatus;
import me.thewro.dermis.entities.repositories.PaymentRepository;
import me.thewro.dermis.entities.repositories.SubscriberRepository;

@Component
public class ApprovePaymentModalOnSubmitEventHandler implements DiscordEventSubscribable<ModalSubmitInteractionEvent> {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private SubscriberRepository subscriberRepository;

    @Override
    public Class<ModalSubmitInteractionEvent> getEventType() {
        return ModalSubmitInteractionEvent.class;
    }

    @Override
    public boolean triggerOn(ModalSubmitInteractionEvent event) {
        return DeveloperCustomId.valueOf(event.getCustomId()).equals(DeveloperCustomId.MODAL_PAYMENT_INPUT);
    }

    @Override
    public void handle(ModalSubmitInteractionEvent event) {
        try {
            event.deferReply().block();
        } catch (Exception e) {
            
        }

        LocalDateTime currentTime = LocalDateTime.now();

        List<MessageComponent> messageComponents = event.getComponents();
        String messageIdTextInput = messageComponents.get(0).getData().components().get().get(0).value().get();
        String amountTextInput = messageComponents.get(1).getData().components().get().get(0).value().get();
        double amount = Double.valueOf(amountTextInput);
        Payment payment = paymentRepository.findById(messageIdTextInput).get();
        payment.setStatus(PaymentStatus.APPROVED);
        Subscriber subscriber = payment.getSubscriber();
        subscriber.setBalance(subscriber.getBalance() + amount);
        
        paymentRepository.save(payment);
        subscriberRepository.save(subscriber);

        User discordUserSubscriber = App.gatewayDiscordClient.getUserById(Snowflake.of(payment.getPayeeId())).block();
        
        EmbedCreateSpec embedCreateSpec = EmbedCreateSpec.builder()
            .title("Your payment has been successfully approved.")
            .addField("Balance: ", String.valueOf(subscriber.getBalance()), false)
            .build();

        PrivateChannel discordUserSubscriberPrivateChannel = discordUserSubscriber.getPrivateChannel().block();
        discordUserSubscriberPrivateChannel.createMessage(embedCreateSpec).block();

        EmbedCreateSpec embedCreateSpec2 = EmbedCreateSpec.builder()
            .title("👨‍💼  New Spotify Subscription Request")
            .thumbnail(discordUserSubscriber.getAvatarUrl())
            .addField("Requester: ", 
                            discordUserSubscriber.getMention() + " (" + discordUserSubscriber.getTag() + ")", 
                            false)
            .footer("Payment sent" + 
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
