package me.thewro.dermis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import me.thewro.dermis.config.DiscordConfig;
import me.thewro.dermis.events.subscribers.DiscordEventSubscriber;

@SpringBootApplication
public class App {

    @Autowired
    ApplicationContext applicationContext;

	@Autowired
	DiscordConfig discordConfig;

	@Autowired
	DiscordEventSubscriber discordEventSubscriber;

	private static DiscordClient discordClient;
    private static GatewayDiscordClient gatewayDiscordClient;
    private static ApplicationInfo applicationInfo;
    private static User owner;
    private static PrivateChannel ownerPrivateChannel;
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	void run() {
		String clientId = discordConfig.getToken();
		discordClient = DiscordClient.create(clientId);
		gatewayDiscordClient = discordClient.login().block();

		applicationInfo = gatewayDiscordClient.getApplicationInfo().block();
		owner = applicationInfo.getOwner().block();
		ownerPrivateChannel = owner.getPrivateChannel().block();

		discordEventSubscriber.subscribe(gatewayDiscordClient);

		gatewayDiscordClient.onDisconnect().block();
		
	}
}
