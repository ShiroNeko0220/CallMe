package fr.miage.toulouse.callme.statistiquesms.repository;

import fr.miage.toulouse.callme.statistiquesms.entity.StatCours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatCoursRepository extends JpaRepository<StatCours, Long> {
    List<StatCours> findByNiveauCible(Integer niveauCible);
}
