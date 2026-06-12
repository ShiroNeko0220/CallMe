package fr.miage.toulouse.callme.statistiquesms.repository;

import fr.miage.toulouse.callme.statistiquesms.entity.StatResultat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StatResultatRepository extends JpaRepository<StatResultat, String> {
    List<StatResultat> findByEleveId(Long eleveId);
    List<StatResultat> findByEleveIdAndCompetitionDateBetween(Long eleveId, LocalDate debut, LocalDate fin);
}
