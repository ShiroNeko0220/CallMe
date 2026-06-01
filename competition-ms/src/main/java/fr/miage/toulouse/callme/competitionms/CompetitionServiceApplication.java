package fr.miage.toulouse.callme.competitionms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "fr.miage.toulouse.callme")
public class CompetitionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompetitionServiceApplication.class, args);
    }

}
