package me.thewro.dermis.entities.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import me.thewro.dermis.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    
}
