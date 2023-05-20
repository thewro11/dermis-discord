package me.thewro.dermis.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.component.TextInput;
import discord4j.core.object.component.SelectMenu.Option;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionPresentModalSpec;
import me.thewro.dermis.App;
import me.thewro.dermis.entities.Payment;
import me.thewro.dermis.entities.enums.DeveloperCustomId;
import me.thewro.dermis.entities.repositories.PaymentRepository;

@Component
public class ApprovePaymentEventHandler implements DiscordEventSubscribable<ButtonInteractionEvent> {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Class<ButtonInteractionEvent> getEventType() {
        return ButtonInteractionEvent.class;
    }

    @Override
    public boolean triggerOn(ButtonInteractionEvent event) {
        return DeveloperCustomId.valueOf(event.getCustomId()).equals(DeveloperCustomId.BUTTON_APPROVE_PAYMENT);
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        String messageId = event.getInteraction().getMessageId().get().asString();
        Payment payment = paymentRepository.findById(messageId).get();
        User discordUserSubscriber = App.gatewayDiscordClient.getUserById(Snowflake.of(payment.getPayeeId())).block();

        event.presentModal(InteractionPresentModalSpec.create()
            .withTitle("Update Payment for " + discordUserSubscriber.getTag())
            .withCustomId(DeveloperCustomId.MODAL_PAYMENT_INPUT.name())
            .withComponents(
                ActionRow.of(TextInput.small(DeveloperCustomId.TEXT_INPUT_MESSAGE_ID.name(), "Message ID").prefilled(messageId).placeholder(messageId).required()),
                ActionRow.of(TextInput.small(DeveloperCustomId.TEXT_INPUT_PAYMENT.name(), "Input Paid Amount (฿)").placeholder("34.00").required())
            )
        )
        .block();

    }
    
}
