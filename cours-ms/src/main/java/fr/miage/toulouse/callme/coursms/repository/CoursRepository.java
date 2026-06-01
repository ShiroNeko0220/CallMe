package fr.miage.toulouse.callme.coursms.repository;

import fr.miage.toulouse.callme.coursms.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.*;

import java.util.List;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {

    List<Cours> findByEnseignantId(Long enseignantId);

    List<Cours> findByNiveauCible(int niveauCible);
}