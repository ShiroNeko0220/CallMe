package fr.miage.toulouse.callme.utilisateurms.service;

import fr.miage.toulouse.callme.libcommun.ApiException;
import fr.miage.toulouse.callme.utilisateurms.DTO.AdminUpdateUtilisateurRequest;
import fr.miage.toulouse.callme.utilisateurms.DTO.UpdateUtilisateurRequest;
import fr.miage.toulouse.callme.utilisateurms.DTO.UtilisateurCreationRequest;
import fr.miage.toulouse.callme.utilisateurms.DTO.UtilisateurResponse;
import fr.miage.toulouse.callme.utilisateurms.config.RabbitMQConfig;
import fr.miage.toulouse.callme.utilisateurms.entity.Adresse;
import fr.miage.toulouse.callme.utilisateurms.entity.Utilisateur;
import fr.miage.toulouse.callme.utilisateurms.enums.Role;
import fr.miage.toulouse.callme.utilisateurms.repository.UtilisateurRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UtilisateurService {
    private final UtilisateurRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    public UtilisateurService(UtilisateurRepository repo, BCryptPasswordEncoder passwordEncoder, RabbitTemplate rabbitTemplate) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
    }

    public UtilisateurResponse creer(UtilisateurCreationRequest request) {
        if (repo.existsByIdConnexionLogin(request.getIdConnexion().getLogin())) {
            throw new ApiException(HttpStatus.CONFLICT, "Utilisateur existant");
        }
        Utilisateur u = new Utilisateur();
        u.setNom(request.getNom());
        u.setPrenom(request.getPrenom());
        u.setEmail(request.getEmail());
        request.getIdConnexion().setMdp(passwordEncoder.encode(request.getIdConnexion().getMdp()));
        u.setIdConnexion(request.getIdConnexion());
        u.setAdresse(request.getAdresse());
        // Inscription publique : un nouveau compte est toujours un membre niveau 1.
        // Le rôle et le niveau ne peuvent être changés que via PATCH /utilisateurs/{id}/admin.
        u.setRole(Role.MEMBRE);
        u.setNiveauExpertise(1);
        Utilisateur saved = repo.save(u);
        publishUtilisateur(saved);
        return toDTO(saved);
    }

    private Utilisateur findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur non existant"));
    }

    public UtilisateurResponse consulter(Long id) {
        return toDTO(findById(id));
    }

    public List<UtilisateurResponse> lister() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    public UtilisateurResponse modifier(Long id, Long utilisateurConnecteId, UpdateUtilisateurRequest request) {
        if (utilisateurConnecteId == null || !utilisateurConnecteId.equals(id)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous ne pouvez modifier que votre propre profil");
        }

        Utilisateur u = findById(id);
        appliquerInfosPersonnelles(u, request.getNom(), request.getPrenom(), request.getEmail(), request.getVille(), request.getPays());

        Utilisateur saved = repo.save(u);
        publishUtilisateur(saved);
        return toDTO(saved);
    }

    public UtilisateurResponse modifierAdmin(Long id, AdminUpdateUtilisateurRequest request) {
        Utilisateur u = findById(id);
        appliquerInfosPersonnelles(u, request.getNom(), request.getPrenom(), request.getEmail(), request.getVille(), request.getPays());

        if (request.getNiveauExpertise() != null) u.setNiveauExpertise(request.getNiveauExpertise());
        if (request.getRole() != null)            u.setRole(request.getRole());

        Utilisateur saved = repo.save(u);
        publishUtilisateur(saved);
        return toDTO(saved);
    }

    private void appliquerInfosPersonnelles(Utilisateur u, String nom, String prenom, String email, String ville, String pays) {
        if (nom != null)    u.setNom(nom);
        if (prenom != null) u.setPrenom(prenom);
        if (email != null)  u.setEmail(email);

        if (ville != null || pays != null) {
            if (u.getAdresse() == null) u.setAdresse(new Adresse());
            if (ville != null) u.getAdresse().setVille(ville);
            if (pays  != null) u.getAdresse().setPays(pays);
        }
    }

    private void publishUtilisateur(Utilisateur u) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.KEY_UTILISATEUR,
                    Map.of(
                        "id", u.getId(),
                        "niveauExpertise", u.getNiveauExpertise(),
                        "role", u.getRole().name()
                    ));
        } catch (Exception e) {
            // non-bloquant
        }
    }

    public void publierTousLesUtilisateurs() {
        repo.findAll().forEach(this::publishUtilisateur);
    }

    private UtilisateurResponse toDTO(Utilisateur u) {
        return UtilisateurResponse.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .login(u.getIdConnexion() != null ? u.getIdConnexion().getLogin() : null)
                .ville(u.getAdresse() != null ? u.getAdresse().getVille() : null)
                .pays(u.getAdresse() != null ? u.getAdresse().getPays() : null)
                .niveauExpertise(u.getNiveauExpertise())
                .role(u.getRole())
                .build();
    }

    public boolean enseignantApte(Long id, int niveau) {
        Utilisateur u = findById(id);
        return u.getRole() == Role.ENSEIGNANT && u.getNiveauExpertise() >= niveau;
    }

    public int getNiveauUtilisateur(Long id) {
        return findById(id).getNiveauExpertise();
    }

    public Role getRoleUtilisateur(Long id) {
        return findById(id).getRole();
    }

    public void supprimer(Long id) {
        if (!repo.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Utilisateur non existant");
        }
        repo.deleteById(id);
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    public UtilisateurResponse login(String login, String mdp) {
        Utilisateur u = repo.findByIdConnexionLogin(login)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects"));
        if (!passwordEncoder.matches(mdp, u.getIdConnexion().getMdp())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Identifiants incorrects");
        }
        return toDTO(u);
    }
}
