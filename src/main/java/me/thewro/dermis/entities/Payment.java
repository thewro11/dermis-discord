package me.thewro.dermis.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.thewro.dermis.entities.enums.PaymentStatus;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Payment {
    @Id
    @NonNull
    private String messageId;

    @NonNull
    @ManyToOne
    private Subscriber subscriber;

    @NonNull
    private String payerId;
    
    @NonNull
    private String payeeId;

    @NonNull
    private String paymentUrl;

    @NonNull
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.NEW;

}
