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
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTests {

    @Mock private PresenceRepository repository;
    @Mock private BadgeClient badgeClient;
    @Mock private CoursClient coursClient;
    @Mock private UtilisateurClient utilisateurClient;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PresenceService service;

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

    // --- badger ---

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
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        PresenceResponse response = service.badger(request);

        assertThat(response.getIdPresence()).isEqualTo(100L);
        assertThat(response.getIdPorteur()).isEqualTo(5L);
        assertThat(response.getIdCours()).isEqualTo(10L);
        assertThat(response.getIdBadge()).isEqualTo(1L);
        verify(repository).save(any());
        verify(rabbitTemplate).convertAndSend(anyString(), eq("presence.enregistree"), any(Object.class));
    }

    @Test
    void badger_badgeStatutDisponible_refus() {
        BadgeClient.BadgeResponse disponible = new BadgeClient.BadgeResponse();
        disponible.setIdBadge(1L);
        disponible.setStatut("DISPONIBLE");
        disponible.setIdPorteur(null);

        when(badgeClient.getBadgeParId(1L)).thenReturn(disponible);

        assertThatThrownBy(() -> service.badger(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Badge non associé à un élève");

        verify(repository, never()).save(any());
    }

    @Test
    void badger_badgeAssocie_maisIdPorteurNull_refus() {
        badgeAssocie.setIdPorteur(null);
        when(badgeClient.getBadgeParId(1L)).thenReturn(badgeAssocie);

        assertThatThrownBy(() -> service.badger(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Badge non associé à un élève");
    }

    @Test
    void badger_badgeInexistant() {
        when(badgeClient.getBadgeParId(1L)).thenThrow(new RuntimeException("feign error"));

        assertThatThrownBy(() -> service.badger(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Badge non existant");
    }

    @Test
    void badger_coursInexistant() {
        when(badgeClient.getBadgeParId(1L)).thenReturn(badgeAssocie);
        when(coursClient.getCours(10L)).thenThrow(new RuntimeException("feign error"));

        assertThatThrownBy(() -> service.badger(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Cours non existant");
    }

    @Test
    void badger_niveauEleveIncompatible() {
        when(badgeClient.getBadgeParId(1L)).thenReturn(badgeAssocie);
        when(coursClient.getCours(10L)).thenReturn(cours);
        when(utilisateurClient.getNiveauUtilisateur(5L)).thenReturn(2); // cours niveau 3

        assertThatThrownBy(() -> service.badger(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("L'élève n'appartient pas au niveau du cours");

        verify(repository, never()).save(any());
    }

    @Test
    void badger_niveauEleveNull_refus() {
        when(badgeClient.getBadgeParId(1L)).thenReturn(badgeAssocie);
        when(coursClient.getCours(10L)).thenReturn(cours);
        when(utilisateurClient.getNiveauUtilisateur(5L)).thenReturn(null);

        assertThatThrownBy(() -> service.badger(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("L'élève n'appartient pas au niveau du cours");
    }

    @Test
    void badger_presenceDejaEnregistree() {
        when(badgeClient.getBadgeParId(1L)).thenReturn(badgeAssocie);
        when(coursClient.getCours(10L)).thenReturn(cours);
        when(utilisateurClient.getNiveauUtilisateur(5L)).thenReturn(3);
        when(repository.existsByIdPorteurAndIdCours(5L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> service.badger(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Présence déjà enregistrée pour cet élève et ce cours");

        verify(repository, never()).save(any());
    }

    // --- listerParEleve ---

    @Test
    void listerParEleve_sansFiltreDates() {
        Presence p = new Presence();
        p.setIdPresence(1L); p.setIdPorteur(5L); p.setIdCours(10L);
        p.setIdBadge(1L); p.setDateBadgeage(LocalDateTime.now());

        when(repository.findByIdPorteur(5L)).thenReturn(List.of(p));

        List<PresenceResponse> result = service.listerParEleve(5L, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdPorteur()).isEqualTo(5L);
        verify(repository).findByIdPorteur(5L);
        verify(repository, never()).findByIdPorteurAndDateBadgeageBetween(any(), any(), any());
    }

    @Test
    void listerParEleve_avecFiltreDebutFin() {
        LocalDate debut = LocalDate.now().minusDays(7);
        LocalDate fin = LocalDate.now();
        Presence p = new Presence();
        p.setIdPresence(1L); p.setIdPorteur(5L); p.setIdCours(10L);
        p.setIdBadge(1L); p.setDateBadgeage(LocalDateTime.now().minusDays(3));

        when(repository.findByIdPorteurAndDateBadgeageBetween(
                eq(5L), eq(debut.atStartOfDay()), eq(fin.atTime(LocalTime.MAX))))
                .thenReturn(List.of(p));

        List<PresenceResponse> result = service.listerParEleve(5L, debut, fin);

        assertThat(result).hasSize(1);
        verify(repository).findByIdPorteurAndDateBadgeageBetween(eq(5L), any(), any());
        verify(repository, never()).findByIdPorteur(any());
    }

    @Test
    void listerParEleve_listeVide() {
        when(repository.findByIdPorteur(99L)).thenReturn(List.of());

        assertThat(service.listerParEleve(99L, null, null)).isEmpty();
    }

    // --- listerParCours ---

    @Test
    void listerParCours_succes() {
        Presence p = new Presence();
        p.setIdPresence(1L); p.setIdPorteur(5L); p.setIdCours(10L);
        p.setIdBadge(1L); p.setDateBadgeage(LocalDateTime.now());

        when(repository.findByIdCours(10L)).thenReturn(List.of(p));

        List<PresenceResponse> result = service.listerParCours(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdCours()).isEqualTo(10L);
    }

    // --- compterParCours ---

    @Test
    void compterParCours_retourneNombre() {
        when(repository.countByIdCours(10L)).thenReturn(4L);

        assertThat(service.compterParCours(10L)).isEqualTo(4L);
    }

    // --- lister ---

    @Test
    void lister_retourneTous() {
        Presence p1 = new Presence(); p1.setIdPresence(1L); p1.setIdPorteur(5L);
        p1.setIdCours(10L); p1.setIdBadge(1L); p1.setDateBadgeage(LocalDateTime.now());
        Presence p2 = new Presence(); p2.setIdPresence(2L); p2.setIdPorteur(6L);
        p2.setIdCours(11L); p2.setIdBadge(2L); p2.setDateBadgeage(LocalDateTime.now());

        when(repository.findAll()).thenReturn(List.of(p1, p2));

        assertThat(service.lister()).hasSize(2);
    }

    // --- compterParTousCours ---

    @Test
    void compterParTousCours_retourneMap() {
        Object[] row1 = {10L, 3L};
        Object[] row2 = {11L, 5L};
        when(repository.countGroupByIdCours()).thenReturn(List.of(row1, row2));

        var result = service.compterParTousCours();

        assertThat(result).hasSize(2);
        assertThat(result.get(10L)).isEqualTo(3L);
        assertThat(result.get(11L)).isEqualTo(5L);
    }
}
