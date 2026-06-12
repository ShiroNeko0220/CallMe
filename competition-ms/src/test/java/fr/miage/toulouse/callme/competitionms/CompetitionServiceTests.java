package fr.miage.toulouse.callme.competitionms;

import fr.miage.toulouse.callme.competitionms.DTO.CompetitionRequest;
import fr.miage.toulouse.callme.competitionms.DTO.CompetitionResponse;
import fr.miage.toulouse.callme.competitionms.DTO.ResultatRequest;
import fr.miage.toulouse.callme.competitionms.DTO.ResultatResponse;
import fr.miage.toulouse.callme.competitionms.clients.UtilisateurClient;
import fr.miage.toulouse.callme.competitionms.entity.Competition;
import fr.miage.toulouse.callme.competitionms.entity.Resultat;
import fr.miage.toulouse.callme.competitionms.repository.CompetitionRepository;
import fr.miage.toulouse.callme.competitionms.repository.ResultatRepository;
import fr.miage.toulouse.callme.competitionms.service.CompetitionService;
import fr.miage.toulouse.callme.libcommun.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompetitionServiceTests {

    @Mock private CompetitionRepository competitionRepo;
    @Mock private ResultatRepository resultatRepo;
    @Mock private UtilisateurClient utilisateurClient;
    @Mock private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CompetitionService service;

    private CompetitionRequest request;
    private Competition baseCompetition;

    @BeforeEach
    void setUp() {
        request = new CompetitionRequest();
        request.setTitre("Championnat régional");
        request.setDate(LocalDate.now().plusDays(10));
        request.setHeureDebut(LocalTime.of(10, 0));
        request.setDuree(90);
        request.setNiveauCible(3);
        request.setEnseignantId(1L);

        baseCompetition = new Competition();
        baseCompetition.setId("comp-1");
        baseCompetition.setTitre("Championnat régional");
        baseCompetition.setDate(request.getDate());
        baseCompetition.setHeureDebut(request.getHeureDebut());
        baseCompetition.setDuree(90);
        baseCompetition.setNiveauCible(3);
        baseCompetition.setEnseignantId(1L);
    }

    @Test
    void creer_succes() {
        when(utilisateurClient.enseignantApte(1L, 3)).thenReturn(true);
        when(competitionRepo.save(any())).thenReturn(baseCompetition);

        CompetitionResponse result = service.creer(request);

        assertNotNull(result);
        assertEquals("comp-1", result.getId());
        assertEquals("Championnat régional", result.getTitre());
        assertEquals(3, result.getNiveauCible());
        verify(utilisateurClient).enseignantApte(1L, 3);
        verify(competitionRepo).save(any());
    }

    @Test
    void creer_dateTropProche() {
        request.setDate(LocalDate.now().plusDays(3));

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("La date doit être supérieure à 7 jours calendaires");
    }

    @Test
    void creer_niveauInvalide() {
        request.setNiveauCible(6);

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Niveau invalide entre 1 et 5");
    }

    @Test
    void creer_enseignantNonApte() {
        when(utilisateurClient.enseignantApte(1L, 3)).thenReturn(false);

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Enseignant non apte pour ce niveau");
    }

    @Test
    void consulter_succes() {
        when(competitionRepo.findById("comp-1")).thenReturn(Optional.of(baseCompetition));

        CompetitionResponse result = service.consulter("comp-1");

        assertEquals("comp-1", result.getId());
    }

    @Test
    void consulter_introuvable() {
        when(competitionRepo.findById("xxx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consulter("xxx"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Competition non existante");
    }

    @Test
    void ajouterResultat_succes() {
        ResultatRequest req = new ResultatRequest();
        req.setEleveId(5L);
        req.setEnseignantId(1L);
        req.setNote(new BigDecimal("7.5"));

        Resultat savedResultat = new Resultat();
        savedResultat.setId("res-1");
        savedResultat.setCompetitionId("comp-1");
        savedResultat.setEleveId(5L);
        savedResultat.setEnseignantId(1L);
        savedResultat.setNote(new BigDecimal("7.5"));
        savedResultat.setCompetitionDate(baseCompetition.getDate());

        when(competitionRepo.findById("comp-1")).thenReturn(Optional.of(baseCompetition));
        when(utilisateurClient.getRoleUtilisateur(1L)).thenReturn("ENSEIGNANT");
        when(utilisateurClient.getNiveauUtilisateur(5L)).thenReturn(3);
        when(resultatRepo.findByCompetitionIdAndEleveId("comp-1", 5L)).thenReturn(Optional.empty());
        when(resultatRepo.save(any())).thenReturn(savedResultat);

        ResultatResponse result = service.ajouterResultat("comp-1", req);

        assertNotNull(result);
        assertEquals(new BigDecimal("7.5"), result.getNote());
        verify(resultatRepo).save(any());
    }

    @Test
    void ajouterResultat_noteTropHaute() {
        ResultatRequest req = new ResultatRequest();
        req.setEleveId(5L);
        req.setEnseignantId(1L);
        req.setNote(new BigDecimal("11"));

        when(competitionRepo.findById("comp-1")).thenReturn(Optional.of(baseCompetition));

        assertThatThrownBy(() -> service.ajouterResultat("comp-1", req))
                .isInstanceOf(ApiException.class)
                .hasMessage("La note doit être comprise entre 0 et 10");
    }

    @Test
    void ajouterResultat_precisonTropFine() {
        ResultatRequest req = new ResultatRequest();
        req.setEleveId(5L);
        req.setEnseignantId(1L);
        req.setNote(new BigDecimal("7.55"));

        when(competitionRepo.findById("comp-1")).thenReturn(Optional.of(baseCompetition));

        assertThatThrownBy(() -> service.ajouterResultat("comp-1", req))
                .isInstanceOf(ApiException.class)
                .hasMessage("La note doit avoir une précision maximale au dixième");
    }

    @Test
    void ajouterResultat_nonEnseignant_interdit() {
        ResultatRequest req = new ResultatRequest();
        req.setEleveId(5L);
        req.setEnseignantId(2L);
        req.setNote(new BigDecimal("8.0"));

        when(competitionRepo.findById("comp-1")).thenReturn(Optional.of(baseCompetition));
        when(utilisateurClient.getRoleUtilisateur(2L)).thenReturn("MEMBRE");

        assertThatThrownBy(() -> service.ajouterResultat("comp-1", req))
                .isInstanceOf(ApiException.class)
                .hasMessage("Seul un enseignant peut saisir un résultat");
    }

    @Test
    void ajouterResultat_doublonInterdit() {
        ResultatRequest req = new ResultatRequest();
        req.setEleveId(5L);
        req.setEnseignantId(1L);
        req.setNote(new BigDecimal("6.0"));

        Resultat existant = new Resultat();
        when(competitionRepo.findById("comp-1")).thenReturn(Optional.of(baseCompetition));
        when(utilisateurClient.getRoleUtilisateur(1L)).thenReturn("ENSEIGNANT");
        when(utilisateurClient.getNiveauUtilisateur(5L)).thenReturn(3);
        when(resultatRepo.findByCompetitionIdAndEleveId("comp-1", 5L)).thenReturn(Optional.of(existant));

        assertThatThrownBy(() -> service.ajouterResultat("comp-1", req))
                .isInstanceOf(ApiException.class)
                .hasMessage("Résultat déjà saisi pour cet élève");
    }

    @Test
    void supprimer_succes() {
        when(competitionRepo.findById("comp-1")).thenReturn(Optional.of(baseCompetition));

        service.supprimer("comp-1");

        verify(competitionRepo).delete(baseCompetition);
    }

    @Test
    void compterParNiveau_succes() {
        when(competitionRepo.countByNiveauCible(3)).thenReturn(5L);

        assertEquals(5L, service.compterParNiveau(3));
    }
}
