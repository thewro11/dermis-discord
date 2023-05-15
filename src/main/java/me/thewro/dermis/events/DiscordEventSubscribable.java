package me.thewro.dermis.events;

import discord4j.core.event.domain.Event;

public interface DiscordEventSubscribable<T extends Event> {

    Class<T> getEventType();

    boolean triggerOn(T event);

    void handle(T event);

}
