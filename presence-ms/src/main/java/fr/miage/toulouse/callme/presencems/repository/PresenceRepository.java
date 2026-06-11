package fr.miage.toulouse.callme.presencems.repository;

import fr.miage.toulouse.callme.presencems.entity.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PresenceRepository extends JpaRepository<Presence, Long> {
    List<Presence> findByIdPorteur(Long idPorteur);
    List<Presence> findByIdPorteurAndDateBadgeageBetween(Long idPorteur, LocalDateTime debut, LocalDateTime fin);
    List<Presence> findByIdCours(Long idCours);
    boolean existsByIdPorteurAndIdCours(Long idPorteur, Long idCours);
    long countByIdCours(Long idCours);

    @Query("SELECT p.idCours, COUNT(p) FROM Presence p GROUP BY p.idCours")
    List<Object[]> countGroupByIdCours();
}
