package fr.miage.toulouse.callme.statistiquesms;

import fr.miage.toulouse.callme.libcommun.ApiException;
import fr.miage.toulouse.callme.statistiquesms.DTO.CoursPresenceEleveResponse;
import fr.miage.toulouse.callme.statistiquesms.DTO.CoursStatistiqueResponse;
import fr.miage.toulouse.callme.statistiquesms.DTO.PresenceStatResponse;
import fr.miage.toulouse.callme.statistiquesms.DTO.ResultatStatResponse;
import fr.miage.toulouse.callme.statistiquesms.entity.*;
import fr.miage.toulouse.callme.statistiquesms.repository.*;
import fr.miage.toulouse.callme.statistiquesms.service.StatistiquesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatistiquesServiceTests {

    @Mock private StatCoursRepository coursRepo;
    @Mock private StatPresenceRepository presenceRepo;
    @Mock private StatCompetitionRepository competitionRepo;
    @Mock private StatResultatRepository resultatRepo;
    @Mock private StatEleveRepository eleveRepo;

    @InjectMocks
    private StatistiquesService service;

    // --- statistiquesCours ---

    @Test
    void statistiquesCours_aucunCours() {
        when(coursRepo.findAll()).thenReturn(List.of());

        CoursStatistiqueResponse result = service.statistiquesCours();

        assertThat(result.getNombreCours()).isEqualTo(0);
        assertThat(result.getNombreMoyenElevesPresents()).isEqualTo(0.0);
    }

    @Test
    void statistiquesCours_moyenneCalculeeCorrectement() {
        StatCours c1 = StatCours.builder().id(1L).niveauCible(3).build();
        StatCours c2 = StatCours.builder().id(2L).niveauCible(3).build();

        when(coursRepo.findAll()).thenReturn(List.of(c1, c2));
        when(presenceRepo.countByIdCours(1L)).thenReturn(4L);
        when(presenceRepo.countByIdCours(2L)).thenReturn(6L);

        CoursStatistiqueResponse result = service.statistiquesCours();

        assertThat(result.getNombreCours()).isEqualTo(2);
        assertThat(result.getNombreMoyenElevesPresents()).isEqualTo(5.0); // (4+6)/2
    }

    @Test
    void statistiquesCours_unSeulCours_sansPresence() {
        StatCours c = StatCours.builder().id(1L).niveauCible(2).build();
        when(coursRepo.findAll()).thenReturn(List.of(c));
        when(presenceRepo.countByIdCours(1L)).thenReturn(0L);

        CoursStatistiqueResponse result = service.statistiquesCours();

        assertThat(result.getNombreCours()).isEqualTo(1);
        assertThat(result.getNombreMoyenElevesPresents()).isEqualTo(0.0);
    }

    // --- elevesPresentsCours ---

    @Test
    void elevesPresentsCours_succes() {
        StatPresence p = StatPresence.builder().id(1L).idPorteur(5L).idCours(10L)
                .dateBadgeage(LocalDateTime.now()).build();
        when(presenceRepo.findByIdCours(10L)).thenReturn(List.of(p));

        List<PresenceStatResponse> result = service.elevesPresentsCours(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdPorteur()).isEqualTo(5L);
        assertThat(result.get(0).getIdCours()).isEqualTo(10L);
    }

    @Test
    void elevesPresentsCours_coursVide() {
        when(presenceRepo.findByIdCours(99L)).thenReturn(List.of());

        assertThat(service.elevesPresentsCours(99L)).isEmpty();
    }

    // --- coursPourEleveAvecPresence ---

    @Test
    void coursPourEleveAvecPresence_eleveNonExistant() {
        when(eleveRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.coursPourEleveAvecPresence(99L, null, null))
                .isInstanceOf(ApiException.class)
                .hasMessage("Élève non existant");
    }

    @Test
    void coursPourEleveAvecPresence_avecDatesExplicites_marquePresent() {
        LocalDate debut = LocalDate.now().minusDays(10);
        LocalDate fin = LocalDate.now().plusDays(10);

        StatEleve eleve = StatEleve.builder().id(5L).niveauExpertise(3).build();
        StatCours coursPresent = StatCours.builder().id(10L).titre("Cours A")
                .date(LocalDate.now()).heureDebut(LocalTime.of(9, 0)).niveauCible(3).build();
        StatCours coursAbsent = StatCours.builder().id(11L).titre("Cours B")
                .date(LocalDate.now()).heureDebut(LocalTime.of(14, 0)).niveauCible(3).build();
        StatPresence presence = StatPresence.builder().id(1L).idPorteur(5L).idCours(10L)
                .dateBadgeage(LocalDateTime.now()).build();

        when(eleveRepo.findById(5L)).thenReturn(Optional.of(eleve));
        when(coursRepo.findByNiveauCible(3)).thenReturn(List.of(coursPresent, coursAbsent));
        when(presenceRepo.findByIdPorteurAndDateBadgeageBetween(
                eq(5L), eq(debut.atStartOfDay()), any())).thenReturn(List.of(presence));

        List<CoursPresenceEleveResponse> result = service.coursPourEleveAvecPresence(5L, debut, fin);

        assertThat(result).hasSize(2);
        assertThat(result.stream().filter(r -> r.getIdCours().equals(10L)).findFirst().orElseThrow().isPresent()).isTrue();
        assertThat(result.stream().filter(r -> r.getIdCours().equals(11L)).findFirst().orElseThrow().isPresent()).isFalse();
    }

    @Test
    void coursPourEleveAvecPresence_datesNulles_defaultAujourdhui() {
        // Quand debut/fin sont null, le service default à LocalDate.now()
        // Seuls les cours du jour sont inclus dans le filtre
        LocalDate aujourd_hui = LocalDate.now();

        StatEleve eleve = StatEleve.builder().id(5L).niveauExpertise(2).build();
        StatCours coursAujourdhui = StatCours.builder().id(10L).titre("Cours aujourd'hui")
                .date(aujourd_hui).heureDebut(LocalTime.of(10, 0)).niveauCible(2).build();
        StatCours coursPassé = StatCours.builder().id(11L).titre("Cours passé")
                .date(aujourd_hui.minusDays(5)).heureDebut(LocalTime.of(10, 0)).niveauCible(2).build();

        when(eleveRepo.findById(5L)).thenReturn(Optional.of(eleve));
        when(coursRepo.findByNiveauCible(2)).thenReturn(List.of(coursAujourdhui, coursPassé));
        when(presenceRepo.findByIdPorteurAndDateBadgeageBetween(eq(5L), any(), any())).thenReturn(List.of());

        List<CoursPresenceEleveResponse> result = service.coursPourEleveAvecPresence(5L, null, null);

        // Seul le cours d'aujourd'hui passe le filtre debut/fin = today
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdCours()).isEqualTo(10L);
    }

    // --- nombreCompetitionsParNiveau ---

    @Test
    void nombreCompetitionsParNiveau_succes() {
        when(competitionRepo.countByNiveauCible(3)).thenReturn(7L);

        assertThat(service.nombreCompetitionsParNiveau(3)).isEqualTo(7L);
    }

    @Test
    void nombreCompetitionsParNiveau_aucune() {
        when(competitionRepo.countByNiveauCible(5)).thenReturn(0L);

        assertThat(service.nombreCompetitionsParNiveau(5)).isEqualTo(0L);
    }

    // --- resultatsCompetitionEleve ---

    @Test
    void resultatsCompetitionEleve_avecDatesExplicites() {
        LocalDate debut = LocalDate.now().minusDays(30);
        LocalDate fin = LocalDate.now();

        StatResultat r = StatResultat.builder()
                .id("res-1").eleveId(5L).enseignantId(2L)
                .note(new BigDecimal("8.0")).competitionId("comp-1")
                .competitionDate(LocalDate.now().minusDays(5)).build();

        when(resultatRepo.findByEleveIdAndCompetitionDateBetween(5L, debut, fin))
                .thenReturn(List.of(r));

        List<ResultatStatResponse> result = service.resultatsCompetitionEleve(5L, debut, fin);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNote()).isEqualByComparingTo(new BigDecimal("8.0"));
        assertThat(result.get(0).getCompetitionId()).isEqualTo("comp-1");
        assertThat(result.get(0).getEleveId()).isEqualTo(5L);
    }

    @Test
    void resultatsCompetitionEleve_datesNulles_defaultAujourdhui() {
        // Quand debut/fin sont null, le service utilise LocalDate.now() pour les deux
        LocalDate today = LocalDate.now();

        when(resultatRepo.findByEleveIdAndCompetitionDateBetween(eq(5L), eq(today), eq(today)))
                .thenReturn(List.of());

        List<ResultatStatResponse> result = service.resultatsCompetitionEleve(5L, null, null);

        assertThat(result).isEmpty();
        verify(resultatRepo).findByEleveIdAndCompetitionDateBetween(5L, today, today);
    }

    @Test
    void resultatsCompetitionEleve_aucunResultat() {
        LocalDate debut = LocalDate.now().minusDays(7);
        LocalDate fin = LocalDate.now();
        when(resultatRepo.findByEleveIdAndCompetitionDateBetween(5L, debut, fin)).thenReturn(List.of());

        assertThat(service.resultatsCompetitionEleve(5L, debut, fin)).isEmpty();
    }
}
