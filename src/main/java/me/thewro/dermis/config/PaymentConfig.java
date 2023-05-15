package me.thewro.dermis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Data
public class PaymentConfig {
    
    private int invoiceDate;

    private double monthlyPrice;

}
