package me.thewro.dermis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "discord.credentials")
@Data
public class DiscordConfig {

    private String token;

    private String version;

}
