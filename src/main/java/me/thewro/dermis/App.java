package me.thewro.dermis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
	private DiscordConfig discordConfig;

	@Autowired
	private DiscordEventSubscriber discordEventSubscriber;

	public static DiscordClient discordClient;
    public static GatewayDiscordClient gatewayDiscordClient;
    public static ApplicationInfo applicationInfo;
	public static User owner;
	public static PrivateChannel ownerPrivateChannel;
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);

		gatewayDiscordClient.onDisconnect().block();
		System.out.println( String.format("[Dermis] [%s] Dermis is now shutting down.", 
							LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a"))));
	}

	@Bean
	void run() {
		System.out.println(	String.format("[Dermis] [%s] Dermis is now starting.", 
							LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a"))));

		String clientId = discordConfig.getToken();

		discordClient = DiscordClient.create(clientId);
		gatewayDiscordClient = discordClient.login().block();
		applicationInfo = gatewayDiscordClient.getApplicationInfo().block();
		owner = applicationInfo.getOwner().block();
		ownerPrivateChannel = owner.getPrivateChannel().block();

		discordEventSubscriber.subscribe(gatewayDiscordClient);

		System.out.println(	String.format("[Dermis] [%s] Dermis is now ready.", 
							LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu hh:mm:ss a"))));
		
	}
}
