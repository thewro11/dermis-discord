package me.thewro.dermis.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import discord4j.core.object.entity.User;
import me.thewro.dermis.entities.Requester;
import me.thewro.dermis.entities.Subscriber;
import me.thewro.dermis.entities.repositories.RequesterRepository;
import me.thewro.dermis.entities.repositories.SubscriberRepository;

@Service
public class RequesterConsiderationService {
    
    @Autowired
    private RequesterRepository requesterRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private SubscriberService subscriberService;

    public Subscriber approve(User owner, Requester requester, LocalDateTime currentTime) {
        Subscriber subscriber = new Subscriber(requester, owner, currentTime);
        subscriber.setNextPaymentAt(subscriberService.calculateNextDueDateTime(subscriber));

        requesterRepository.delete(requester);
        subscriberRepository.save(subscriber);

        return subscriber;
    }

}
