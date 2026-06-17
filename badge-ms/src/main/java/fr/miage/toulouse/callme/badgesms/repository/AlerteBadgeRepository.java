package fr.miage.toulouse.callme.badgesms.repository;

import fr.miage.toulouse.callme.badgesms.entity.AlerteBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlerteBadgeRepository extends JpaRepository<AlerteBadge, Long> {
    List<AlerteBadge> findByResolueOrderByDateCreationDesc(boolean resolue);
    void deleteByIdEnseignantAndIdActivite(Long idEnseignant, String idActivite);
}