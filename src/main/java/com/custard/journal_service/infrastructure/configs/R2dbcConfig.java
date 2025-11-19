package com.custard.journal_service.infrastructure.configs;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class R2dbcConfig {

    @Autowired
    ConnectionFactory connectionFactory;

}