package fr.miage.toulouse.callme.statistiquesms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "fr.miage.toulouse.callme")
@EnableFeignClients
public class StatistiquesMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatistiquesMsApplication.class, args);
    }

}
