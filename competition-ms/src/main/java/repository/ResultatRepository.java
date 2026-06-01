package repository;

import entity.Resultat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.*;

public interface ResultatRepository extends JpaRepository<Resultat, Long> {

    List<Resultat> findByEleveId(Long eleveId);

    Optional<Resultat> findByCompetitionIdAndEleveId(Long competitionId, Long eleveId);

    List<Resultat> findByCompetitionId(Long competitionId);

    List<Resultat> findByEleveIdAndCompetitionDateBetween(Long eleveId, LocalDate debut, LocalDate fin);
}
