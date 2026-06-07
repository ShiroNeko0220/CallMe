package fr.miage.toulouse.callme.badgesms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(scanBasePackages = "fr.miage.toulouse.callme")
public class BadgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadgeServiceApplication.class, args);
    }

}
