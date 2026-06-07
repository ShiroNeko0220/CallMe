package fr.miage.toulouse.callme.presencems.clients;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cours-ms-presence", url = "${services.cours.url}")
public interface CoursClient {
    @GetMapping("/cours/{id}")
    CoursResponse getCours(@PathVariable("id") Long id);

    @Getter
    @Setter
    class CoursResponse {
        private Long id;
        private String titre;
        private Integer niveauCible;
        private Long enseignantId;
    }
}
