package fr.miage.toulouse.callme.badgesms.messaging;

import fr.miage.toulouse.callme.badgesms.config.RabbitMQConfig;
import fr.miage.toulouse.callme.badgesms.entity.AlerteBadge;
import fr.miage.toulouse.callme.badgesms.entity.Statut;
import fr.miage.toulouse.callme.badgesms.repository.AlerteBadgeRepository;
import fr.miage.toulouse.callme.badgesms.repository.BadgeRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ActivityEventListener {

    private final BadgeRepository badgeRepository;
    private final AlerteBadgeRepository alerteRepository;

    public ActivityEventListener(BadgeRepository badgeRepository, AlerteBadgeRepository alerteRepository) {
        this.badgeRepository = badgeRepository;
        this.alerteRepository = alerteRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURS)
    public void onCoursCree(Map<String, Object> event) {
        traiterEvenement(event, "COURS");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COMPET)
    public void onCompetitionCreee(Map<String, Object> event) {
        traiterEvenement(event, "COMPETITION");
    }

    private void traiterEvenement(Map<String, Object> event, String type) {
        Object enseignantIdObj = event.get("enseignantId");
        if (enseignantIdObj == null) return;

        Long enseignantId = toLong(enseignantIdObj);
        boolean hasBadge = badgeRepository.findByIdPorteur(enseignantId)
                .map(b -> b.getStatut() == Statut.ASSOCIE)
                .orElse(false);

        if (!hasBadge) {
            AlerteBadge alerte = new AlerteBadge();
            alerte.setTypeActivite(type);
            alerte.setIdActivite(String.valueOf(event.get("id")));
            alerte.setTitreActivite(String.valueOf(event.get("titre")));
            alerte.setDateActivite(parseDate(event.get("date")));
            alerte.setIdEnseignant(enseignantId);
            alerte.setDateCreation(LocalDateTime.now());
            alerte.setResolue(false);
            alerteRepository.save(alerte);
        }
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    private LocalDate parseDate(Object val) {
        if (val == null) return null;
        try { return LocalDate.parse(val.toString()); } catch (Exception e) { return null; }
    }
}