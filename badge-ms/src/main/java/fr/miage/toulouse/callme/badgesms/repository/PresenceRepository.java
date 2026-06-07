package fr.miage.toulouse.callme.badgesms.repository;

import fr.miage.toulouse.callme.badgesms.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    List<Presence> findByIdPorteur(Long idPorteur);

    List<Presence> findByIdCours(Long idCours);

    boolean existsByIdBadgeAndIdCours(Long idBadge, Long idCours);
}
