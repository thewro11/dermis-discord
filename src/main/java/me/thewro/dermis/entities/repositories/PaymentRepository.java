package me.thewro.dermis.entities.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import me.thewro.dermis.entities.Payment;
import me.thewro.dermis.entities.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    List<Payment> findByPayeeIdAndStatus(String payeeId, PaymentStatus status);

}
