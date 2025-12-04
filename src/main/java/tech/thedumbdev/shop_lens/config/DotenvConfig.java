package tech.thedumbdev.shop_lens.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {
    static {
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}