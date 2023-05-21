package me.thewro.dermis.entities;

import java.time.LocalDateTime;
import java.util.List;

import discord4j.core.object.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.thewro.dermis.entities.enums.RequesterStatus;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class Subscriber extends Requester {

    public Subscriber(Requester requester, User owner, LocalDateTime currentTime) {
        this.setUserId(requester.getUserId());
        this.setStatus(RequesterStatus.APPROVED);
        this.setRequestedAt(requester.getRequestedAt());
        this.setRequestMessageId(requester.getRequestMessageId());
        this.setGuildId(requester.getGuildId());
        this.setConsideredByOwnerId(owner.getId().asString());
        this.setConsideredAt(currentTime);
    }

    private double balance;

    @OneToMany
    private List<Payment> payments;

}
