package com.custard.journal_service;

import com.custard.journal_service.infrastructure.workers.JournalJobWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class})
@RequiredArgsConstructor
@EnableFeignClients(basePackages = "com.custard.journal_service.infrastructure.clients")
public class JournalServiceApplication implements CommandLineRunner {

    private final JournalJobWorker journalJobWorker;

    public static void main(String[] args) {
        SpringApplication.run(JournalServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        journalJobWorker.start();
    }
}
