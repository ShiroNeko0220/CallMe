package fr.miage.toulouse.callme.utilisateurms.repository;

import fr.miage.toulouse.callme.utilisateurms.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.*;
import java.util.*;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    boolean existsByIdConnexionLogin(String login);

    Optional<Utilisateur> findByIdConnexionLogin(String login);
}
