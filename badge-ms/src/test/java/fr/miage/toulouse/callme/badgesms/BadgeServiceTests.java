package fr.miage.toulouse.callme.badgesms;

import fr.miage.toulouse.callme.badgesms.DTO.BadgeRequest;
import fr.miage.toulouse.callme.badgesms.DTO.BadgeResponse;
import fr.miage.toulouse.callme.badgesms.clients.UtilisateurClient;
import fr.miage.toulouse.callme.badgesms.entity.Badge;
import fr.miage.toulouse.callme.badgesms.entity.Statut;
import fr.miage.toulouse.callme.badgesms.repository.BadgeRepository;
import fr.miage.toulouse.callme.badgesms.service.BadgeService;
import fr.miage.toulouse.callme.libcommun.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTests {

    @Mock private BadgeRepository badgeRepository;
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

    @Test
    void creerBadge_succes() {
        when(badgeRepository.save(any())).thenReturn(badgeDisponible);

        BadgeResponse response = badgeService.creerBadge(new BadgeRequest());

        assertNotNull(response);
        assertEquals(1L, response.getIdBadge());
        assertEquals("DISPONIBLE", response.getStatut());
        verify(badgeRepository).save(any());
    }

    @Test
    void getBadgeById_succes() {
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));

        BadgeResponse response = badgeService.getBadgeById(1L);

        assertEquals(1L, response.getIdBadge());
    }

    @Test
    void getBadgeById_introuvable() {
        when(badgeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> badgeService.getBadgeById(99L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Badge non existant");
    }

    @Test
    void associerBadge_succes() {
        when(utilisateurClient.existsById(10L)).thenReturn(true);
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));
        when(badgeRepository.findByIdPorteur(10L)).thenReturn(Optional.empty());
        when(badgeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BadgeResponse response = badgeService.associerBadge(1L, 10L);

        assertEquals("ASSOCIE", response.getStatut());
        assertEquals(10L, response.getIdPorteur());
    }

    @Test
    void associerBadge_badgeDejaAssocie() {
        when(utilisateurClient.existsById(10L)).thenReturn(true);
        when(badgeRepository.findById(2L)).thenReturn(Optional.of(badgeAssocie));

        assertThatThrownBy(() -> badgeService.associerBadge(2L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Ce badge est déjà associé à un porteur.");
    }

    @Test
    void associerBadge_porteurDejaUnBadge() {
        when(utilisateurClient.existsById(10L)).thenReturn(true);
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));
        when(badgeRepository.findByIdPorteur(10L)).thenReturn(Optional.of(badgeAssocie));

        assertThatThrownBy(() -> badgeService.associerBadge(1L, 10L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Ce porteur possède déjà un badge.");
    }

    @Test
    void associerBadge_porteurInexistant() {
        when(utilisateurClient.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> badgeService.associerBadge(1L, 99L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Aucun utilisateur trouvé avec l'id 99");
    }

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

    @Test
    void supprimerBadge_succes() {
        when(badgeRepository.findById(1L)).thenReturn(Optional.of(badgeDisponible));

        badgeService.supprimerBadge(1L);

        verify(badgeRepository).delete(badgeDisponible);
    }

    @Test
    void supprimerBadge_introuvable() {
        when(badgeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> badgeService.supprimerBadge(99L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Badge non existant");
    }
}
