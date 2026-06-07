package fr.miage.toulouse.callme.statistiquesms.clients;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@FeignClient(name = "competition-ms-statistiques", url = "${services.competition.url}")
public interface CompetitionClient {

    @GetMapping("/competitions/niveau/{niveau}/count")
    Long compterParNiveau(@PathVariable("niveau") int niveau);

    @GetMapping("/competitions/eleve/{eleveId}/resultats")
    List<ResultatResponse> listerResultatsPourEleve(
            @PathVariable("eleveId") Long eleveId,
            @RequestParam(value = "debut", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(value = "fin", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    );

    @Getter
    @Setter
    class ResultatResponse {
        private String id;
        private String competitionId;
        private Long eleveId;
        private Long enseignantId;
        private BigDecimal note;
        private LocalDate competitionDate;
    }

    @Getter
    @Setter
    class CompetitionResponse {
        private String id;
        private String titre;
        private Integer niveauCible;
        private LocalDate date;
        private LocalTime heureDebut;
        private Integer duree;
        private String lieu;
        private Long enseignantId;
    }
}
