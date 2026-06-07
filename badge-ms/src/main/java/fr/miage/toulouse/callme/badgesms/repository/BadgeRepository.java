package fr.miage.toulouse.callme.badgesms.repository;

import fr.miage.toulouse.callme.badgesms.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    Optional<Badge> findByIdPorteur(Long idPorteur);

    List<Badge> findByStatut(Statut statut);
}
