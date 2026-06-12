package fr.miage.toulouse.callme.statistiquesms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "fr.miage.toulouse.callme")
public class StatistiquesMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatistiquesMsApplication.class, args);
    }
}
