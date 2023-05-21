package me.thewro.dermis.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import discord4j.core.object.entity.User;
import me.thewro.dermis.entities.Requester;
import me.thewro.dermis.entities.Subscriber;
import me.thewro.dermis.entities.enums.RequesterStatus;
import me.thewro.dermis.entities.repositories.RequesterRepository;
import me.thewro.dermis.entities.repositories.SubscriberRepository;

@Service
public class RequesterConsiderationService {
    
    @Autowired
    private RequesterRepository requesterRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    public Subscriber approve(User owner, Requester requester, LocalDateTime currentTime) {
        Subscriber subscriber = new Subscriber(requester, owner, currentTime);

        requesterRepository.delete(requester);
        subscriberRepository.save(subscriber);

        return subscriber;
    }

    public void reject(User owner, Requester requester, LocalDateTime currentTime) {
        requester.setConsideredAt(currentTime);
        requester.setConsideredByOwnerId(owner.getId().asString());
        requester.setStatus(RequesterStatus.DENIED);
        requesterRepository.save(requester);
    }

}
