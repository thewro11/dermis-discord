package me.thewro.dermis.entities.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import me.thewro.dermis.entities.Requester;

public interface RequesterRepository extends JpaRepository<Requester, String> {

    Requester findByRequestMessageId(String requestMessageId);

}
