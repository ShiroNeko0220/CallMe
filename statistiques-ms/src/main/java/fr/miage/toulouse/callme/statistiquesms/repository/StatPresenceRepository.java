package fr.miage.toulouse.callme.statistiquesms.repository;

import fr.miage.toulouse.callme.statistiquesms.entity.StatPresence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StatPresenceRepository extends JpaRepository<StatPresence, Long> {
    List<StatPresence> findByIdCours(Long idCours);
    List<StatPresence> findByIdPorteur(Long idPorteur);
    List<StatPresence> findByIdPorteurAndDateBadgeageBetween(Long idPorteur, LocalDateTime debut, LocalDateTime fin);
    List<StatPresence> findByIdPorteurAndDateBadgeageGreaterThanEqual(Long idPorteur, LocalDateTime debut);
    List<StatPresence> findByIdPorteurAndDateBadgeageLessThanEqual(Long idPorteur, LocalDateTime fin);
    long countByIdCours(Long idCours);
}
