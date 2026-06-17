package fr.miage.toulouse.callme.badgesms;

import fr.miage.toulouse.callme.badgesms.DTO.BadgeRequest;
import fr.miage.toulouse.callme.badgesms.DTO.BadgeResponse;
import fr.miage.toulouse.callme.badgesms.clients.UtilisateurClient;
import fr.miage.toulouse.callme.badgesms.entity.AlerteBadge;
import fr.miage.toulouse.callme.badgesms.entity.Badge;
import fr.miage.toulouse.callme.badgesms.entity.Statut;
import fr.miage.toulouse.callme.badgesms.repository.AlerteBadgeRepository;
import fr.miage.toulouse.callme.badgesms.repository.BadgeRepository;
import fr.miage.toulouse.callme.badgesms.service.BadgeService;
import fr.miage.toulouse.callme.libcommun.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTests {

    @Mock private BadgeRepository badgeRepository;
    @Mock private AlerteBadgeRepository alerteRepository;
    @Mock private UtilisateurClient utilisateurClient;

    @InjectMocks
    private BadgeService badgeService;

    private Badge badgeDisponible;
    private Badge badgeAssocie;

    @BeforeEach
    void setUp() {
        badgeDisponible = new Badge();
        badgeDisponible.setIdBadge(1L);
        badgeDisponible.setStatut(Statut.DISPONIBLE);
        badgeDisponible.setDateCreation(LocalDateTime.now());

        badgeAssocie = new Badge();
        badgeAssocie.setIdBadge(2L);
        badgeAssocie.setStatut(Statut.ASSOCIE);
        badgeAssocie.setIdPorteur(10L);
        badgeAssocie.setDateCreation(LocalDateTime.now());
        badgeAssocie.setDateAssociation(LocalDateTime.now());
    }

    // --- creerBadge ---

    @Test
    void creerBadge_succes() {
        when(badgeRepository.save(any())).thenReturn(badgeDisponible);

        BadgeResponse response = badgeService.creerBadge(new BadgeRequest());

        assertNotNull(response);
        assertEquals(1L, response.getIdBadge());
        assertEquals("DISPONIBLE", response.getStatut());
        verify(badgeRepository).save(any());
    }

    // --- getBadgeById ---

    @Test
    void getBadgeById_succes() {
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));

        BadgeResponse response = badgeService.getBadgeById(1L);

        assertEquals(1L, response.getIdBadge());
    }

    @Test
    void getBadgeById_nonExistant() {
        when(badgeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> badgeService.getBadgeById(99L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Badge non existant");
    }

    // --- getBadgeByPorteur ---

    @Test
    void getBadgeByPorteur_succes() {
        when(badgeRepository.findByIdPorteur(10L)).thenReturn(Optional.of(badgeAssocie));

        BadgeResponse response = badgeService.getBadgeByPorteur(10L);

        assertEquals(2L, response.getIdBadge());
        assertEquals(10L, response.getIdPorteur());
        assertEquals("ASSOCIE", response.getStatut());
    }

    @Test
    void getBadgeByPorteur_aucunBadge() {
        when(badgeRepository.findByIdPorteur(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> badgeService.getBadgeByPorteur(99L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Aucun badge associé à ce porteur");
    }

    // --- listerBadges ---

    @Test
    void listerBadges_retourneTous() {
        when(badgeRepository.findAll()).thenReturn(List.of(badgeDisponible, badgeAssocie));

        List<BadgeResponse> result = badgeService.listerBadges();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIdBadge()).isEqualTo(1L);
        assertThat(result.get(1).getIdBadge()).isEqualTo(2L);
        verify(badgeRepository).findAll();
    }

    @Test
    void listerBadges_listeVide() {
        when(badgeRepository.findAll()).thenReturn(List.of());

        List<BadgeResponse> result = badgeService.listerBadges();

        assertThat(result).isEmpty();
    }

    // --- associerBadge ---

    @Test
    void associerBadge_succes() {
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));
        when(badgeRepository.findByIdPorteur(10L)).thenReturn(Optional.empty());
        when(utilisateurClient.existsById(10L)).thenReturn(true);
        when(badgeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(alerteRepository.findByResolueOrderByDateCreationDesc(false)).thenReturn(List.of());

        BadgeResponse response = badgeService.associerBadge(1L, 10L);

        assertEquals("ASSOCIE", response.getStatut());
        assertEquals(10L, response.getIdPorteur());
    }

    @Test
    void associerBadge_succes_resoutAlertesPendantes() {
        AlerteBadge alerte = new AlerteBadge();
        alerte.setId(1L);
        alerte.setIdEnseignant(10L);
        alerte.setTypeActivite("COURS");
        alerte.setIdActivite("cours-1");
        alerte.setTitreActivite("Maths");
        alerte.setDateActivite(LocalDate.now());

        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));
        when(badgeRepository.findByIdPorteur(10L)).thenReturn(Optional.empty());
        when(utilisateurClient.existsById(10L)).thenReturn(true);
        when(badgeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(alerteRepository.findByResolueOrderByDateCreationDesc(false)).thenReturn(List.of(alerte));
        when(alerteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        badgeService.associerBadge(1L, 10L);

        assertTrue(alerte.isResolue());
        verify(alerteRepository).save(alerte);
    }

    @Test
    void associerBadge_badgeDejaAssocie() {
        when(badgeRepository.findById(2L)).thenReturn(Optional.of(badgeAssocie));

        assertThatThrownBy(() -> badgeService.associerBadge(2L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Ce badge est déjà associé à un porteur.");

        verify(badgeRepository, never()).save(any());
    }

    @Test
    void associerBadge_porteurDejaUnBadge() {
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));
        when(badgeRepository.findByIdPorteur(10L)).thenReturn(Optional.of(badgeAssocie));

        assertThatThrownBy(() -> badgeService.associerBadge(1L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Ce porteur possède déjà un badge.");

        verify(badgeRepository, never()).save(any());
    }

    @Test
    void associerBadge_porteurInexistant() {
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));
        when(badgeRepository.findByIdPorteur(99L)).thenReturn(Optional.empty());
        when(utilisateurClient.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> badgeService.associerBadge(1L, 99L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Aucun utilisateur trouvé avec l'id 99");
    }

    @Test
    void associerBadge_serviceUtilisateurIndisponible() {
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));
        when(badgeRepository.findByIdPorteur(10L)).thenReturn(Optional.empty());
        when(utilisateurClient.existsById(10L)).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> badgeService.associerBadge(1L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Impossible de vérifier le membre");
    }

    // --- dissocierBadge ---

    @Test
    void dissocierBadge_succes() {
        when(badgeRepository.findById(2L)).thenReturn(Optional.of(badgeAssocie));
        when(badgeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BadgeResponse response = badgeService.dissocierBadge(2L);

        assertEquals("DISPONIBLE", response.getStatut());
        assertNull(response.getIdPorteur());
    }

    @Test
    void dissocierBadge_badgeNonAssocie() {
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));

        assertThatThrownBy(() -> badgeService.dissocierBadge(1L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Ce badge n'est associé à aucun porteur.");
    }

    // --- supprimerBadge ---

    @Test
    void supprimerBadge_succes() {
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));

        badgeService.supprimerBadge(1L);

        verify(badgeRepository).delete(badgeDisponible);
    }

    @Test
    void supprimerBadge_nonExistant() {
        when(badgeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> badgeService.supprimerBadge(99L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Badge non existant");
    }

    // --- listerAlertes ---

    @Test
    void listerAlertes_retourneAlerteNonResolues() {
        AlerteBadge alerte = new AlerteBadge();
        alerte.setId(1L);
        alerte.setIdEnseignant(5L);
        alerte.setTypeActivite("COURS");
        alerte.setIdActivite("cours-42");
        alerte.setTitreActivite("Judo débutant");
        alerte.setDateActivite(LocalDate.now().plusDays(10));
        alerte.setResolue(false);

        when(alerteRepository.findByResolueOrderByDateCreationDesc(false)).thenReturn(List.of(alerte));

        var result = badgeService.listerAlertes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdEnseignant()).isEqualTo(5L);
        assertThat(result.get(0).isResolue()).isFalse();
    }

    @Test
    void listerAlertes_aucuneAlerte() {
        when(alerteRepository.findByResolueOrderByDateCreationDesc(false)).thenReturn(List.of());

        assertThat(badgeService.listerAlertes()).isEmpty();
    }
}
