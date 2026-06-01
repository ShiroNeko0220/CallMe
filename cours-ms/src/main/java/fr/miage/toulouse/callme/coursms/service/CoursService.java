package fr.miage.toulouse.callme.coursms.service;

import fr.miage.toulouse.callme.coursms.DTO.*;
import fr.miage.toulouse.callme.coursms.clients.UtilisateurClient;
import fr.miage.toulouse.callme.coursms.entity.*;
import fr.miage.toulouse.callme.coursms.repository.*;
import fr.miage.toulouse.callme.libcommun.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CoursService {

    private final CoursRepository repo;
    private final UtilisateurClient utilisateurClient;

    public CoursService(CoursRepository repo, UtilisateurClient utilisateurClient) {
        this.repo = repo;
        this.utilisateurClient = utilisateurClient;
    }

    public Cours creer(CoursRequest request) {
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

        return repo.save(cours);
    }

    public Cours consulter(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cours non existant"));
    }

    public List<Cours> lister() {
        return repo.findAll();
    }

    public List<Cours> listerParEnseignant(Long enseignantId) {
        return repo.findByEnseignantId(enseignantId);
    }

    public List<Cours> listerParNiveau(int niveau) {
        verifierNiveau(niveau);
        return repo.findByNiveauCible(niveau);
    }

//    public List<Cours> listerCreneauxPourEleve(Long eleveId) {
//        Integer niveau = utilisateurClient.getNiveauExpertise(eleveId);
//        return repo.findByNiveauCible(niveau);
//    }

    public Cours modifier(Long id, CoursRequest request) {
        Cours old = consulter(id);

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
            old.setEnseignantId(request.getEnseignantId());
        }

        return repo.save(old);
    }

    public void supprimer(Long id) {
        Cours cours = consulter(id);
        repo.delete(cours);
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
