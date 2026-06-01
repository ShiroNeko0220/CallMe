package repository;

import entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    List<Competition> findByNiveauCible(int niveauCible);

    List<Competition> findByEnseignantId(Long enseignantId);

    List<Competition> findByDateBetween(LocalDate debut, LocalDate fin);

    long countByNiveauCible(int niveauCible);
}
