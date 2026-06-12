package fr.miage.toulouse.callme.utilisateurms.service;

import fr.miage.toulouse.callme.libcommun.ApiException;
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
        u.setRole(request.getRole() != null ? request.getRole() : Role.MEMBRE);
        u.setNiveauExpertise(request.getNiveauExpertise() != null && request.getNiveauExpertise() > 0 ? request.getNiveauExpertise() : 1);
        Utilisateur saved = repo.save(u);
        publishUtilisateur(saved);
        return toDTO(saved);
    }

    private Utilisateur findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    public UtilisateurResponse consulter(Long id) {
        return toDTO(findById(id));
    }

    public List<UtilisateurResponse> lister() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    public UtilisateurResponse modifier(Long id, UpdateUtilisateurRequest request) {
        Utilisateur u = findById(id);

        if (request.getNom() != null)    u.setNom(request.getNom());
        if (request.getPrenom() != null) u.setPrenom(request.getPrenom());
        if (request.getEmail() != null)  u.setEmail(request.getEmail());

        if (request.getVille() != null || request.getPays() != null) {
            if (u.getAdresse() == null) u.setAdresse(new Adresse());
            if (request.getVille() != null) u.getAdresse().setVille(request.getVille());
            if (request.getPays()  != null) u.getAdresse().setPays(request.getPays());
        }

        if (request.getNiveauExpertise() != null) u.setNiveauExpertise(request.getNiveauExpertise());
        if (request.getRole() != null)            u.setRole(request.getRole());

        Utilisateur saved = repo.save(u);
        publishUtilisateur(saved);
        return toDTO(saved);
    }

    private void publishUtilisateur(Utilisateur u) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.KEY_UTILISATEUR,
                    Map.of("id", u.getId(), "niveauExpertise", u.getNiveauExpertise()));
        } catch (Exception e) {
            // non-bloquant : la statistique sera éventuellement cohérente
        }
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
            throw new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
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
