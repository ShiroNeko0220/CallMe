package fr.miage.toulouse.callme.statistiquesms.clients;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@FeignClient(name = "presence-ms", contextId = "presenceStatistiquesClient")
public interface PresenceClient {
    @GetMapping("/presences/cours/{idCours}")
    List<PresenceResponse> listerParCours(@PathVariable("idCours") Long idCours);

    @GetMapping("/presences/cours/{idCours}/count")
    Long compterParCours(@PathVariable("idCours") Long idCours);

    @GetMapping("/presences/cours/counts")
    Map<Long, Long> compterParTousCours();

    @GetMapping("/presences/eleve/{idEleve}")
    List<PresenceResponse> listerParEleve(
            @PathVariable("idEleve") Long idEleve,
            @RequestParam(value = "debut", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(value = "fin", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin);

    @Getter
    @Setter
    class PresenceResponse {
        private Long idPresence;
        private Long idBadge;
        private Long idPorteur;
        private Long idCours;
        private LocalDateTime dateBadgeage;
    }
}
