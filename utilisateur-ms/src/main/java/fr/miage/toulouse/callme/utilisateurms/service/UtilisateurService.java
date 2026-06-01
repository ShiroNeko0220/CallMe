package fr.miage.toulouse.callme.utilisateurms.service;

import fr.miage.toulouse.callme.libcommun.*;
import fr.miage.toulouse.callme.utilisateurms.DTO.UtilisateurCreationRequest;
import fr.miage.toulouse.callme.utilisateurms.entity.Utilisateur;
import fr.miage.toulouse.callme.utilisateurms.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.*;
import java.util.*;

@Service
public class UtilisateurService {
    private final UtilisateurRepository repo;

    public UtilisateurService(UtilisateurRepository repo){
        this.repo=repo;
    }

    public Utilisateur creer(UtilisateurCreationRequest request){
        if (repo.existsByIdConnexionLogin(request.getIdConnexion().getLogin())) {
            throw new ApiException(HttpStatus.CONFLICT, "Utilisateur existant");
        }
        Utilisateur u = new Utilisateur();
        u.setNom(request.getNom());
        u.setPrenom(request.getPrenom());
        u.setEmail(request.getEmail());
        u.setIdConnexion(request.getIdConnexion());
        u.setAdresse(request.getAdresse());
        u.setRole(Role.MEMBRE);
        u.setNiveauExpertise(1);
        return repo.save(u);
    }

    public Utilisateur consulter(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    public List<Utilisateur> lister(){
        return repo.findAll();
    }

    public Utilisateur modifier(Long id, Utilisateur u){
        Utilisateur old=consulter(id);

        if (u.getNom() != null && !u.getNom().equals(old.getNom())) {
            old.setNom(u.getNom());
        }

        if (u.getPrenom() != null && !u.getPrenom().equals(old.getPrenom())) {
            old.setPrenom(u.getPrenom());
        }

        if (u.getEmail() != null && !u.getEmail().equals(old.getEmail())) {
            old.setEmail(u.getEmail());
        }

        if (u.getIdConnexion() != null && !u.getIdConnexion().equals(old.getIdConnexion())) {
            old.setIdConnexion((u.getIdConnexion()));
        }

        if (u.getAdresse() != null && !u.getAdresse().equals(old.getAdresse())) {
            old.setAdresse((u.getAdresse()));
        }

        if(u.getNiveauExpertise() >=1 && u.getNiveauExpertise() <=5 && u.getNiveauExpertise() != old.getNiveauExpertise()) {
            old.setNiveauExpertise(u.getNiveauExpertise());
        }

        if(u.getRole() !=null && old.getRole() != u.getRole()){
            old.setRole(u.getRole());
        }
        return repo.save(old);
    }

    public boolean enseignantApte(Long id,int niveau){
        Utilisateur u = repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur non existant"));
        return u.getRole() == Role.ENSEIGNANT && u.getNiveauExpertise() >= niveau;
    }

    public int getNiveauUtilisateur(Long id){
        Utilisateur u = repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur non existant"));
        return u.getNiveauExpertise();
    }

    public Role getRoleUtilisateur(Long id){
        Utilisateur u = repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur non existant"));
        return u.getRole();
    }
}
