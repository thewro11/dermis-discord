package me.thewro.dermis.entities.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import me.thewro.dermis.entities.Subscriber;


public interface SubscriberRepository extends JpaRepository<Subscriber, String> {

}
