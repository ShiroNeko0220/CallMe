package fr.miage.toulouse.callme.utilisateurms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "fr.miage.toulouse.callme")
public class UtilisateurServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UtilisateurServiceApplication.class, args);
    }

}
