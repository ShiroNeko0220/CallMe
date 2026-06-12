package fr.miage.toulouse.callme.coursms.service;

import fr.miage.toulouse.callme.coursms.DTO.CoursRequest;
import fr.miage.toulouse.callme.coursms.DTO.CoursResponse;
import fr.miage.toulouse.callme.coursms.clients.UtilisateurClient;
import fr.miage.toulouse.callme.coursms.config.RabbitMQConfig;
import fr.miage.toulouse.callme.coursms.entity.*;
import fr.miage.toulouse.callme.coursms.repository.*;
import fr.miage.toulouse.callme.libcommun.ApiException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CoursService {

    private final CoursRepository repo;
    private final UtilisateurClient utilisateurClient;
    private final RabbitTemplate rabbitTemplate;

    public CoursService(CoursRepository repo, UtilisateurClient utilisateurClient, RabbitTemplate rabbitTemplate) {
        this.repo = repo;
        this.utilisateurClient = utilisateurClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    public CoursResponse creer(CoursRequest request) {
        verifierDuree(request.getDuree());
        verifierNiveau(request.getNiveauCible());
        verifierDateCours(request.getDate());

        Boolean apte = utilisateurClient.enseignantApte(
                request.getEnseignantId(),
                request.getNiveauCible()
        );

        if (!Boolean.TRUE.equals(apte)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Enseignant non apte pour ce niveau");
        }

        Cours cours = new Cours();
        cours.setTitre(request.getTitre());
        cours.setDate(request.getDate());
        cours.setHeureDebut(request.getHeureDebut());
        cours.setDuree(request.getDuree());
        cours.setLieu(request.getLieu());
        cours.setNiveauCible(request.getNiveauCible());
        cours.setEnseignantId(request.getEnseignantId());

        Cours saved = repo.save(cours);

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,
            RabbitMQConfig.KEY_COURS,
            Map.of(
                "id", saved.getId(),
                "titre", saved.getTitre(),
                "niveauCible", saved.getNiveauCible(),
                "date", saved.getDate().toString(),
                "heureDebut", saved.getHeureDebut().toString(),
                "duree", saved.getDuree(),
                "enseignantId", saved.getEnseignantId()
            )
        );

        return toDTO(saved);
    }

    private Cours findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cours non existant"));
    }

    public CoursResponse consulter(Long id) {
        return toDTO(findById(id));
    }

    public List<CoursResponse> lister() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    public List<CoursResponse> listerParEnseignant(Long enseignantId) {
        return repo.findByEnseignantId(enseignantId).stream().map(this::toDTO).toList();
    }

    public List<CoursResponse> listerParNiveau(int niveau) {
        verifierNiveau(niveau);
        return repo.findByNiveauCible(niveau).stream().map(this::toDTO).toList();
    }

    public CoursResponse modifier(Long id, CoursRequest request) {
        Cours old = findById(id);

        if (request.getTitre() != null) {
            old.setTitre(request.getTitre());
        }

        if (request.getDate() != null) {
            old.setDate(request.getDate());
        }

        if (request.getHeureDebut() != null) {
            old.setHeureDebut(request.getHeureDebut());
        }

        if (request.getDuree() != null) {
            verifierDuree(request.getDuree());
            old.setDuree(request.getDuree());
        }

        if (request.getLieu() != null) {
            old.setLieu(request.getLieu());
        }

        if (request.getNiveauCible() != null) {
            verifierNiveau(request.getNiveauCible());
            old.setNiveauCible(request.getNiveauCible());
        }

        if (request.getEnseignantId() != null) {
            Boolean apte = utilisateurClient.enseignantApte(request.getEnseignantId(), old.getNiveauCible());
            if (!Boolean.TRUE.equals(apte)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Enseignant non apte pour ce niveau");
            }
            old.setEnseignantId(request.getEnseignantId());
        }

        return toDTO(repo.save(old));
    }

    public void supprimer(Long id) {
        Cours cours = findById(id);
        repo.delete(cours);
    }

    private CoursResponse toDTO(Cours cours) {
        return CoursResponse.builder()
                .id(cours.getId())
                .titre(cours.getTitre())
                .date(cours.getDate())
                .heureDebut(cours.getHeureDebut())
                .duree(cours.getDuree())
                .lieu(cours.getLieu())
                .niveauCible(cours.getNiveauCible())
                .enseignantId(cours.getEnseignantId())
                .build();
    }

    private void verifierDuree(int duree) {
        if (duree < 45) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Durée invalide (45min minimum)");
        }
    }

    private void verifierNiveau(int niveau) {
        if (niveau < 1 || niveau > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Niveau invalide (entre 1 et 5)");
        }
    }

    private void verifierDateCours(LocalDate date) {
        LocalDate dateMin = LocalDate.now().plusDays(7);

        if (!date.isAfter(dateMin)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Date cours doit être supérieure à 7 jours");
        }
    }
}
