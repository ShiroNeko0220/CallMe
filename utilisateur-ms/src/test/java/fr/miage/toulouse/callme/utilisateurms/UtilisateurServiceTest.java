package fr.miage.toulouse.callme.utilisateurms;

import fr.miage.toulouse.callme.libcommun.*;
import fr.miage.toulouse.callme.utilisateurms.DTO.*;
import fr.miage.toulouse.callme.utilisateurms.entity.*;
import fr.miage.toulouse.callme.utilisateurms.repository.UtilisateurRepository;
import fr.miage.toulouse.callme.utilisateurms.service.UtilisateurService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository repo;

    @InjectMocks
    private UtilisateurService service;

    private UtilisateurCreationRequest request;
    private Utilisateur u1;
    private Utilisateur u2;
    private Utilisateur update;

    private IdConnexion idConnexion1;
    private IdConnexion idConnexion2;
    private Adresse adresse1;
    private Adresse adresse2;

    @BeforeEach
    void setUp() {
        // utilisateur 1
        idConnexion1 = new IdConnexion();
        idConnexion1.setLogin("test_login");
        idConnexion1.setMdp("test_mdp");
        adresse1 = new Adresse();
        adresse1.setVille("Toulouse");
        adresse1.setPays("France");
        u1 = new Utilisateur();
        u1.setId(1L);
        u1.setNom("Test");
        u1.setPrenom("Test");
        u1.setEmail("test@test.com");
        u1.setIdConnexion(idConnexion1);
        u1.setAdresse(adresse1);
        u1.setRole(Role.MEMBRE);
        u1.setNiveauExpertise(1);

        // request de création
        request = new UtilisateurCreationRequest();
        request.setNom("Test");
        request.setPrenom("Test");
        request.setEmail("test@test.com");
        request.setIdConnexion(idConnexion1);
        request.setAdresse(adresse1);

        // utilisateur 2
        idConnexion2 = new IdConnexion();
        idConnexion2.setLogin("user2_login");
        idConnexion2.setMdp("user2_mdp");
        adresse2 = new Adresse();
        adresse2.setVille("Paris");
        adresse2.setPays("France");
        u2 = new Utilisateur();
        u2.setId(2L);
        u2.setNom("User 2");
        u2.setPrenom("Deux");
        u2.setEmail("user2@test.com");
        u2.setIdConnexion(idConnexion2);
        u2.setAdresse(adresse2);
        u2.setRole(Role.MEMBRE);
        u2.setNiveauExpertise(2);

        // pour les tests de modification
        update = new Utilisateur();
        update.setNom("NouveauNom");
        update.setPrenom("NouveauPrenom");
        update.setEmail("nouveau@test.com");
        update.setNiveauExpertise(3);
    }

    @Test
    void creer_utilisateurNonExistant() {
        when(repo.existsByIdConnexionLogin("test_login")).thenReturn(false);
        when(repo.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        Utilisateur resultat = service.creer(request);

        assertThat(resultat.getId()).isEqualTo(1L);
        assertThat(resultat.getNom()).isEqualTo("Test");
        assertThat(resultat.getPrenom()).isEqualTo("Test");
        assertThat(resultat.getEmail()).isEqualTo("test@test.com");
        assertThat(resultat.getAdresse().getVille()).isEqualTo("Toulouse");
        assertThat(resultat.getAdresse().getPays()).isEqualTo("France");
        assertThat(resultat.getIdConnexion().getLogin()).isEqualTo("test_login");
        assertThat(resultat.getIdConnexion().getMdp()).isEqualTo("test_mdp");
        assertThat(resultat.getRole()).isEqualTo(Role.MEMBRE);
        assertThat(resultat.getNiveauExpertise()).isEqualTo(1);

        verify(repo).existsByIdConnexionLogin("test_login");
        verify(repo).save(any(Utilisateur.class));
    }

    @Test
    void creer_utilisateurExistant() {
        when(repo.existsByIdConnexionLogin("test_login")).thenReturn(true);

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("Utilisateur existant");

        verify(repo).existsByIdConnexionLogin("test_login");
        verify(repo, never()).save(any(Utilisateur.class));
    }

    @Test
    void consulter_utilisateurExistant() {
        when(repo.findById(1L)).thenReturn(Optional.of(u1));
        Utilisateur resultat = service.consulter(1L);

        assertThat(resultat.getId()).isEqualTo(1L);
        assertThat(resultat.getNom()).isEqualTo("Test");
        assertThat(resultat.getPrenom()).isEqualTo("Test");
        assertThat(resultat.getEmail()).isEqualTo("test@test.com");

        verify(repo).findById(1L);
    }

    @Test
    void consulter_utilisateurNonExistant() {
        when(repo.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consulter(5L))
                .isInstanceOf(ApiException.class)
                .hasMessage("UtilisateurIntrouvable");

        verify(repo).findById(5L);
    }

    @Test
    void lister() {
        when(repo.findAll()).thenReturn(List.of(u1, u2));
        List<Utilisateur> resultat = service.lister();

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0)).isEqualTo(u1);
        assertThat(resultat.get(1)).isEqualTo(u2);
        assertThat(resultat.get(0).getNom()).isEqualTo("Test");
        assertThat(resultat.get(1).getNom()).isEqualTo("User 2");

        verify(repo).findAll();
    }

    @Test
    void modifier_utilisateurNonExistant() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.modifier(99L, update))
                .isInstanceOf(ApiException.class)
                .hasMessage("Utilisateur introuvable");

        verify(repo).findById(99L);
        verify(repo, never()).save(any(Utilisateur.class));
    }

    @Test
    void modifier_utilisateurExistant() {
        when(repo.findById(1L)).thenReturn(Optional.of(u1));
        when(repo.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Utilisateur resultat = service.modifier(1L, update);

        assertThat(resultat.getId()).isEqualTo(1L);
        assertThat(resultat.getNom()).isEqualTo("NouveauNom");
        assertThat(resultat.getPrenom()).isEqualTo("NouveauPrenom");
        assertThat(resultat.getEmail()).isEqualTo("nouveau@test.com");
        assertThat(resultat.getNiveauExpertise()).isEqualTo(3);

        assertThat(resultat.getAdresse().getVille()).isEqualTo("Toulouse");
        assertThat(resultat.getAdresse().getPays()).isEqualTo("France");
        assertThat(resultat.getIdConnexion().getLogin()).isEqualTo("test_login");
        assertThat(resultat.getRole()).isEqualTo(Role.MEMBRE);

        verify(repo).findById(1L);
        verify(repo).save(u1);
    }
}