package fr.miage.toulouse.callme.presencems.messaging;

import fr.miage.toulouse.callme.presencems.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CoursEventListener {

    private static final Logger log = LoggerFactory.getLogger(CoursEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURS)
    public void onCoursCree(Map<String, Object> event) {
        Long coursId   = Long.parseLong(event.get("coursId").toString());
        Object niveau  = event.get("niveauCible");
        log.info("[AMQP] Nouveau cours reçu : id={} niveau={}", coursId, niveau);
        // Extension possible : pré-charger les élèves du niveau pour optimiser les badgeages
    }
}
