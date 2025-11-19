package com.custard.journal_service.infrastructure.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfig {

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting() // Optional: for pretty-printed JSON
                .serializeNulls()    // Optional: include null values
                .setDateFormat("yyyy-MM-dd HH:mm:ss") // Optional: date format
                .create();
    }
}