package fr.miage.toulouse.callme.coursms;

import fr.miage.toulouse.callme.coursms.DTO.CoursRequest;
import fr.miage.toulouse.callme.coursms.clients.UtilisateurClient;
import fr.miage.toulouse.callme.coursms.entity.Cours;
import fr.miage.toulouse.callme.coursms.repository.CoursRepository;
import fr.miage.toulouse.callme.coursms.service.CoursService;
import fr.miage.toulouse.callme.libcommun.ApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoursServiceTest {

    @Mock
    private CoursRepository repo;

    @Mock
    private UtilisateurClient utilisateurClient;

    @InjectMocks
    private CoursService service;

    private CoursRequest request;
    
    private Cours baseCours;

    @BeforeEach
    void setUp() {
        request = new CoursRequest();
        request.setTitre("Mathématiques");
        request.setDate(LocalDate.now().plusDays(10));
        request.setHeureDebut(LocalTime.of(14, 0));
        request.setDuree(60);
        request.setLieu("Salle 101");
        request.setNiveauCible(3);
        request.setEnseignantId(1L);
        
        baseCours = new Cours();
        baseCours.setId(100L);
        baseCours.setTitre(request.getTitre());
        baseCours.setDate(request.getDate());
        baseCours.setHeureDebut(request.getHeureDebut());
        baseCours.setDuree(request.getDuree());
        baseCours.setLieu(request.getLieu());
        baseCours.setNiveauCible(request.getNiveauCible());
        baseCours.setEnseignantId(request.getEnseignantId());
    }

    @Test
    void creer_succes() {
        when(utilisateurClient.enseignantApte(1L, 3)).thenReturn(true);
        when(repo.save(any(Cours.class))).thenReturn(baseCours);
        
        Cours resultat = service.creer(request);
        
        assertNotNull(resultat);
        assertEquals(100L, resultat.getId());
        assertEquals("Mathématiques", resultat.getTitre());
        assertEquals(60, resultat.getDuree());
        assertEquals("Salle 101", resultat.getLieu());
        assertEquals(3, resultat.getNiveauCible());
        assertEquals(1L, resultat.getEnseignantId());
        assertEquals(LocalDate.now().plusDays(10), resultat.getDate());
        assertEquals(LocalTime.of(14, 0), resultat.getHeureDebut());
        
        verify(utilisateurClient).enseignantApte(1L, 3);
        verify(repo).save(any(Cours.class));
    }

    @Test
    void creer_DureeInvalide() {
        request.setDuree(30); 
        
        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Durée invalide (45min minimum)");
    }

    @Test
    void creer_NiveauInvalide() {
        request.setNiveauCible(6);

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Niveau invalide (entre 1 et 5)");
    }

    @Test
    void creer_DateTropProche() {
        request.setDate(LocalDate.now().plusDays(5));

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Date cours doit être supérieure à 7 jours");
    }

    @Test
    void creer_EnseignantNonApte() {
        when(utilisateurClient.enseignantApte(1L, 3)).thenReturn(false);

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Enseignant non apte pour ce niveau");
    }

    @Test
    void consulter_succes() {
        when(repo.findById(100L)).thenReturn(Optional.of(baseCours));

        Cours resultat = service.consulter(100L);

        assertNotNull(resultat);
        assertEquals(100L, resultat.getId());
        assertEquals("Mathématiques", resultat.getTitre());
        assertEquals(60, resultat.getDuree());
        assertEquals("Salle 101", resultat.getLieu());
        assertEquals(3, resultat.getNiveauCible());
        assertEquals(1L, resultat.getEnseignantId());
        assertEquals(LocalDate.now().plusDays(10), resultat.getDate());
        assertEquals(LocalTime.of(14, 0), resultat.getHeureDebut());
    }

    @Test
    void consulter_coursInexistant() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consulter(999L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Cours non existant");
    }

    @Test
    void lister_succes() {
        when(repo.findAll()).thenReturn(List.of(baseCours));

        List<Cours> resultat = service.lister();

        assertEquals(1, resultat.size());

        verify(repo).findAll();
    }

    @Test
    void listerParNiveau_succes() {
        when(repo.findByNiveauCible(3)).thenReturn(List.of(baseCours));

        List<Cours> resultat = service.listerParNiveau(3);

        assertEquals(1, resultat.size());
        verify(repo).findByNiveauCible(3);
    }

    @Test
    void listerParNiveau_NiveauInvalide() {
        assertThatThrownBy(() -> service.listerParNiveau(0))
                .isInstanceOf(ApiException.class)
                .hasMessage("Niveau invalide (entre 1 et 5)");
    }


    @Test
    void modifier_succes() {
        when(repo.findById(100L)).thenReturn(Optional.of(baseCours));

        CoursRequest updateRequest = new CoursRequest();
        updateRequest.setTitre("Nouveau Titre");
        updateRequest.setDuree(90);

        when(repo.save(any(Cours.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cours resultat = service.modifier(100L, updateRequest);

        assertNotNull(resultat);
        assertEquals("Nouveau Titre", resultat.getTitre());
        assertEquals(90, resultat.getDuree());
        assertEquals("Salle 101", resultat.getLieu());
        assertEquals(3, resultat.getNiveauCible());
        assertEquals(1L, resultat.getEnseignantId());
        assertEquals(LocalDate.now().plusDays(10), resultat.getDate());
        assertEquals(LocalTime.of(14, 0), resultat.getHeureDebut());
        verify(repo).save(baseCours);
    }

    @Test
    void modifier_DureeInvalide() {
        when(repo.findById(100L)).thenReturn(Optional.of(baseCours));

        CoursRequest updateRequest = new CoursRequest();
        updateRequest.setDuree(20);

        assertThatThrownBy(() -> service.modifier(100L, updateRequest));

        verify(repo, never()).save(any(Cours.class));
    }

    @Test
    void supprimer_succes() {
        when(repo.findById(100L)).thenReturn(Optional.of(baseCours));

        service.supprimer(100L);

        verify(repo).delete(baseCours);
    }

    @Test
    void supprimer_IdInexistant() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.supprimer(999L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Cours non existant");

        verify(repo, never()).delete(any(Cours.class));

    }
}