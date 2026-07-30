package tn.finix.ledgerengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LedgerEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerEngineApplication.class, args);
    }

}
