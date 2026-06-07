package fr.miage.toulouse.callme.competitionms.repository;

import fr.miage.toulouse.callme.competitionms.entity.Resultat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.*;

public interface ResultatRepository extends MongoRepository<Resultat, String> {

    List<Resultat> findByEleveId(Long eleveId);

    Optional<Resultat> findByCompetitionIdAndEleveId(String competitionId, Long eleveId);

    List<Resultat> findByCompetitionId(String competitionId);

    List<Resultat> findByEleveIdAndCompetitionDateBetween(Long eleveId, LocalDate debut, LocalDate fin);
}
