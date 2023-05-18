package me.thewro.dermis.entities.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import me.thewro.dermis.entities.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
}
