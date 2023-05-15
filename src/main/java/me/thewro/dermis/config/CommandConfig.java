package me.thewro.dermis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "discord.commands")
@Data
public class CommandConfig {
    
    private String register;

    private String status;

}
