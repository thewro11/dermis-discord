package me.thewro.dermis.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.thewro.dermis.entities.enums.RequesterStatus;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Requester {

    @Id
    @NonNull
    private String userId;
    
    @NonNull
    private String guildId;

    @NonNull
    private String requestMessageId;

    @Enumerated(EnumType.STRING)
    private RequesterStatus status = RequesterStatus.NEW;

    @NonNull
    private LocalDateTime requestedAt;

    private LocalDateTime consideredAt;

    private String consideredByOwnerId;

}
