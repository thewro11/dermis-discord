package me.thewro.dermis.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
public class ActivityLog {
    
    @Id
    private long id;

    @Nonnull
    private String action;

}
