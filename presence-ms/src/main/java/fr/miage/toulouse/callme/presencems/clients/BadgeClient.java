package fr.miage.toulouse.callme.presencems.clients;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "badge-ms-presence", url = "${services.badge.url}")
public interface BadgeClient {
    @GetMapping("/badges/{idBadge}")
    BadgeResponse getBadgeParId(@PathVariable("idBadge") Long idBadge);

    @Getter
    @Setter
    class BadgeResponse {
        private Long idBadge;
        private Long idPorteur;
        private String statut;
    }
}
