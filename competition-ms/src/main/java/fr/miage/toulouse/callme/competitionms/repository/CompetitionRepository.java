package fr.miage.toulouse.callme.competitionms.repository;

import fr.miage.toulouse.callme.competitionms.entity.Competition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface CompetitionRepository extends MongoRepository<Competition, String> {

    List<Competition> findByNiveauCible(int niveauCible);

    List<Competition> findByEnseignantId(Long enseignantId);

    List<Competition> findByDateBetween(LocalDate debut, LocalDate fin);

    long countByNiveauCible(int niveauCible);
}
