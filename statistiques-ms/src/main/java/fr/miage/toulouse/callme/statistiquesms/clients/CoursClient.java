package fr.miage.toulouse.callme.statistiquesms.clients;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@FeignClient(name = "cours-ms", contextId = "coursStatistiquesClient")
public interface CoursClient {
    @GetMapping("/cours")
    List<CoursResponse> listerCours();

    @GetMapping("/cours/niveau/{niveau}")
    List<CoursResponse> listerParNiveau(@PathVariable("niveau") int niveau);

    @Getter
    @Setter
    class CoursResponse {
        private Long id;
        private String titre;
        private LocalDate date;
        private LocalTime heureDebut;
        private Integer niveauCible;
        private Long enseignantId;
    }
}
