package fr.miage.toulouse.callme.statistiquesms.repository;

import fr.miage.toulouse.callme.statistiquesms.entity.StatCompetition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatCompetitionRepository extends JpaRepository<StatCompetition, String> {
    long countByNiveauCible(Integer niveauCible);
}
