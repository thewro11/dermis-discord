package me.thewro.dermis.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import me.thewro.dermis.entities.enums.RequesterStatus;

@Entity
@Data
public class Requester {
    
    @Id
    private String userId;
    
    private String guildId;

    private String requestMessageId;

    @Enumerated(EnumType.STRING)
    private RequesterStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

}
