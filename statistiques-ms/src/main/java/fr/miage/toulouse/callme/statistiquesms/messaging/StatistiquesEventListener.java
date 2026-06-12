package fr.miage.toulouse.callme.statistiquesms.messaging;

import fr.miage.toulouse.callme.statistiquesms.config.RabbitMQConfig;
import fr.miage.toulouse.callme.statistiquesms.entity.*;
import fr.miage.toulouse.callme.statistiquesms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Component
public class StatistiquesEventListener {

    private static final Logger log = LoggerFactory.getLogger(StatistiquesEventListener.class);

    private final StatCoursRepository coursRepo;
    private final StatPresenceRepository presenceRepo;
    private final StatCompetitionRepository competitionRepo;
    private final StatResultatRepository resultatRepo;
    private final StatEleveRepository eleveRepo;

    public StatistiquesEventListener(StatCoursRepository coursRepo,
                                     StatPresenceRepository presenceRepo,
                                     StatCompetitionRepository competitionRepo,
                                     StatResultatRepository resultatRepo,
                                     StatEleveRepository eleveRepo) {
        this.coursRepo = coursRepo;
        this.presenceRepo = presenceRepo;
        this.competitionRepo = competitionRepo;
        this.resultatRepo = resultatRepo;
        this.eleveRepo = eleveRepo;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURS)
    public void onCours(Map<String, Object> event) {
        try {
            StatCours cours = StatCours.builder()
                    .id(toLong(event.get("id")))
                    .titre((String) event.get("titre"))
                    .niveauCible(toInt(event.get("niveauCible")))
                    .date(LocalDate.parse(event.get("date").toString()))
                    .heureDebut(LocalTime.parse(event.get("heureDebut").toString()))
                    .duree(toInt(event.get("duree")))
                    .enseignantId(toLong(event.get("enseignantId")))
                    .build();
            coursRepo.save(cours);
            log.info("[STAT] Cours enregistré id={}", cours.getId());
        } catch (Exception e) {
            log.error("[STAT] Erreur traitement event cours: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PRESENCE)
    public void onPresence(Map<String, Object> event) {
        try {
            StatPresence presence = StatPresence.builder()
                    .id(toLong(event.get("id")))
                    .idPorteur(toLong(event.get("idPorteur")))
                    .idCours(toLong(event.get("idCours")))
                    .dateBadgeage(LocalDateTime.parse(event.get("dateBadgeage").toString()))
                    .build();
            presenceRepo.save(presence);
            log.info("[STAT] Présence enregistrée id={}", presence.getId());
        } catch (Exception e) {
            log.error("[STAT] Erreur traitement event présence: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COMPETITION)
    public void onCompetition(Map<String, Object> event) {
        try {
            StatCompetition competition = StatCompetition.builder()
                    .id(event.get("id").toString())
                    .titre((String) event.get("titre"))
                    .niveauCible(toInt(event.get("niveauCible")))
                    .date(LocalDate.parse(event.get("date").toString()))
                    .build();
            competitionRepo.save(competition);
            log.info("[STAT] Compétition enregistrée id={}", competition.getId());
        } catch (Exception e) {
            log.error("[STAT] Erreur traitement event compétition: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RESULTAT)
    public void onResultat(Map<String, Object> event) {
        try {
            StatResultat resultat = StatResultat.builder()
                    .id(event.get("id").toString())
                    .competitionId(event.get("competitionId").toString())
                    .eleveId(toLong(event.get("eleveId")))
                    .enseignantId(toLong(event.get("enseignantId")))
                    .note(new BigDecimal(event.get("note").toString()))
                    .competitionDate(LocalDate.parse(event.get("competitionDate").toString()))
                    .build();
            resultatRepo.save(resultat);
            log.info("[STAT] Résultat enregistré id={}", resultat.getId());
        } catch (Exception e) {
            log.error("[STAT] Erreur traitement event résultat: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_UTILISATEUR)
    public void onUtilisateur(Map<String, Object> event) {
        try {
            StatEleve eleve = StatEleve.builder()
                    .id(toLong(event.get("id")))
                    .niveauExpertise(toInt(event.get("niveauExpertise")))
                    .build();
            eleveRepo.save(eleve);
            log.info("[STAT] Élève mis à jour id={}", eleve.getId());
        } catch (Exception e) {
            log.error("[STAT] Erreur traitement event utilisateur: {}", e.getMessage());
        }
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        return Long.parseLong(val.toString());
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        return Integer.parseInt(val.toString());
    }
}
