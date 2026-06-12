package fr.miage.toulouse.callme.presencems;

import fr.miage.toulouse.callme.libcommun.ApiException;
import fr.miage.toulouse.callme.presencems.DTO.BadgeageRequest;
import fr.miage.toulouse.callme.presencems.DTO.PresenceResponse;
import fr.miage.toulouse.callme.presencems.clients.BadgeClient;
import fr.miage.toulouse.callme.presencems.clients.CoursClient;
import fr.miage.toulouse.callme.presencems.clients.UtilisateurClient;
import fr.miage.toulouse.callme.presencems.entity.Presence;
import fr.miage.toulouse.callme.presencems.repository.PresenceRepository;
import fr.miage.toulouse.callme.presencems.service.PresenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTests {

    @Mock private PresenceRepository repository;
    @Mock private BadgeClient badgeClient;
    @Mock private CoursClient coursClient;
    @Mock private UtilisateurClient utilisateurClient;
    @Mock private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PresenceService presenceService;

    private BadgeageRequest request;
    private BadgeClient.BadgeResponse badgeAssocie;
    private CoursClient.CoursResponse cours;

    @BeforeEach
    void setUp() {
        request = new BadgeageRequest();
        request.setIdBadge(1L);
        request.setIdCours(10L);

        badgeAssocie = new BadgeClient.BadgeResponse();
        badgeAssocie.setIdBadge(1L);
        badgeAssocie.setIdPorteur(5L);
        badgeAssocie.setStatut("ASSOCIE");

        cours = new CoursClient.CoursResponse();
        cours.setId(10L);
        cours.setTitre("Cours Judo N3");
        cours.setNiveauCible(3);
        cours.setEnseignantId(2L);
    }

    @Test
    void badger_succes() {
        Presence saved = new Presence();
        saved.setIdPresence(100L);
        saved.setIdBadge(1L);
        saved.setIdPorteur(5L);
        saved.setIdCours(10L);
        saved.setDateBadgeage(LocalDateTime.now());

        when(badgeClient.getBadgeParId(1L)).thenReturn(badgeAssocie);
        when(coursClient.getCours(10L)).thenReturn(cours);
        when(utilisateurClient.getNiveauUtilisateur(5L)).thenReturn(3);
        when(repository.existsByIdPorteurAndIdCours(5L, 10L)).thenReturn(false);
        when(repository.save(any())).thenReturn(saved);

        PresenceResponse response = presenceService.badger(request);

        assertNotNull(response);
        assertEquals(100L, response.getIdPresence());
        assertEquals(5L, response.getIdPorteur());
        assertEquals(10L, response.getIdCours());
        verify(repository).save(any());
    }

    @Test
    void badger_badgeNonAssocie() {
        BadgeClient.BadgeResponse badgeDisponible = new BadgeClient.BadgeResponse();
        badgeDisponible.setIdBadge(1L);
        badgeDisponible.setStatut("DISPONIBLE");

        when(badgeClient.getBadgeParId(1L)).thenReturn(badgeDisponible);

        assertThatThrownBy(() -> presenceService.badger(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Badge non associé à un élève");
    }

    @Test
    void badger_niveauMismatch() {
        when(badgeClient.getBadgeParId(1L)).thenReturn(badgeAssocie);
        when(coursClient.getCours(10L)).thenReturn(cours);
        when(utilisateurClient.getNiveauUtilisateur(5L)).thenReturn(2); // élève niveau 2, cours niveau 3

        assertThatThrownBy(() -> presenceService.badger(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("L'élève n'appartient pas au niveau du cours");
    }

    @Test
    void badger_presenceDejaEnregistree() {
        when(badgeClient.getBadgeParId(1L)).thenReturn(badgeAssocie);
        when(coursClient.getCours(10L)).thenReturn(cours);
        when(utilisateurClient.getNiveauUtilisateur(5L)).thenReturn(3);
        when(repository.existsByIdPorteurAndIdCours(5L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> presenceService.badger(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Présence déjà enregistrée pour cet élève et ce cours");
    }

    @Test
    void listerParEleve_succes() {
        Presence p = new Presence();
        p.setIdPresence(1L);
        p.setIdPorteur(5L);
        p.setIdCours(10L);
        p.setIdBadge(1L);
        p.setDateBadgeage(LocalDateTime.now());

        when(repository.findByIdPorteur(5L)).thenReturn(List.of(p));

        List<PresenceResponse> result = presenceService.listerParEleve(5L, null, null);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getIdPorteur());
    }

    @Test
    void listerParCours_succes() {
        Presence p = new Presence();
        p.setIdPresence(1L);
        p.setIdPorteur(5L);
        p.setIdCours(10L);
        p.setIdBadge(1L);
        p.setDateBadgeage(LocalDateTime.now());

        when(repository.findByIdCours(10L)).thenReturn(List.of(p));

        List<PresenceResponse> result = presenceService.listerParCours(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getIdCours());
    }

    @Test
    void compterParCours_succes() {
        when(repository.countByIdCours(10L)).thenReturn(3L);

        assertEquals(3L, presenceService.compterParCours(10L));
    }
}
