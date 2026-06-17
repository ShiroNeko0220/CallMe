package fr.miage.toulouse.callme.utilisateurms.messaging;

import fr.miage.toulouse.callme.utilisateurms.service.UtilisateurService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Publie tous les utilisateurs au démarrage pour que statistiques-ms
 * initialise sa table stat_eleve dès le boot via l'event utilisateur.modifie.
 */
@Component
public class UtilisateurStartupPublisher {

    private final UtilisateurService service;

    public UtilisateurStartupPublisher(UtilisateurService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        service.publierTousLesUtilisateurs();
    }
}