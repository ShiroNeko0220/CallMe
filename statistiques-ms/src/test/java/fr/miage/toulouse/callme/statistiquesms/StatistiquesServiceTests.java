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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void statistiquesCours_aucunCours() {
        when(coursRepo.findAll()).thenReturn(List.of());

        CoursStatistiqueResponse result = service.statistiquesCours();

        assertEquals(0, result.getNombreCours());
        assertEquals(0.0, result.getNombreMoyenElevesPresents());
    }

    @Test
    void statistiquesCours_avecPresences() {
        StatCours c1 = StatCours.builder().id(1L).niveauCible(3).build();
        StatCours c2 = StatCours.builder().id(2L).niveauCible(3).build();

        when(coursRepo.findAll()).thenReturn(List.of(c1, c2));
        when(presenceRepo.countByIdCours(1L)).thenReturn(4L);
        when(presenceRepo.countByIdCours(2L)).thenReturn(6L);

        CoursStatistiqueResponse result = service.statistiquesCours();

        assertEquals(2, result.getNombreCours());
        assertEquals(5.0, result.getNombreMoyenElevesPresents());
    }

    @Test
    void elevesPresentsCours_succes() {
        StatPresence p = StatPresence.builder().id(1L).idPorteur(5L).idCours(10L).build();
        when(presenceRepo.findByIdCours(10L)).thenReturn(List.of(p));

        List<PresenceStatResponse> result = service.elevesPresentsCours(10L);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getIdPorteur());
    }

    @Test
    void coursPourEleveAvecPresence_eleveIntrouvable() {
        when(eleveRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.coursPourEleveAvecPresence(99L, null, null))
                .isInstanceOf(ApiException.class)
                .hasMessage("Élève introuvable");
    }

    @Test
    void coursPourEleveAvecPresence_succes() {
        StatEleve eleve = StatEleve.builder().id(5L).niveauExpertise(3).build();
        StatCours c1 = StatCours.builder().id(10L).titre("Cours 1")
                .date(LocalDate.of(2026, 3, 10)).heureDebut(LocalTime.of(9, 0)).niveauCible(3).build();
        StatCours c2 = StatCours.builder().id(11L).titre("Cours 2")
                .date(LocalDate.of(2026, 4, 5)).heureDebut(LocalTime.of(14, 0)).niveauCible(3).build();
        StatPresence presence = StatPresence.builder().id(1L).idPorteur(5L).idCours(10L).build();

        when(eleveRepo.findById(5L)).thenReturn(Optional.of(eleve));
        when(coursRepo.findByNiveauCible(3)).thenReturn(List.of(c1, c2));
        when(presenceRepo.findByIdPorteur(5L)).thenReturn(List.of(presence));

        List<CoursPresenceEleveResponse> result = service.coursPourEleveAvecPresence(5L, null, null);

        assertEquals(2, result.size());
        assertTrue(result.stream().filter(r -> r.getIdCours().equals(10L)).findFirst().orElseThrow().isPresent());
        assertFalse(result.stream().filter(r -> r.getIdCours().equals(11L)).findFirst().orElseThrow().isPresent());
    }

    @Test
    void nombreCompetitionsParNiveau_succes() {
        when(competitionRepo.countByNiveauCible(3)).thenReturn(7L);

        assertEquals(7L, service.nombreCompetitionsParNiveau(3));
    }

    @Test
    void resultatsCompetitionEleve_succes() {
        StatResultat r = StatResultat.builder()
                .id("res-1").eleveId(5L).note(new BigDecimal("8.0"))
                .competitionId("comp-1").competitionDate(LocalDate.now()).build();
        when(resultatRepo.findByEleveId(5L)).thenReturn(List.of(r));

        List<ResultatStatResponse> result = service.resultatsCompetitionEleve(5L, null, null);

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("8.0"), result.get(0).getNote());
    }
}
