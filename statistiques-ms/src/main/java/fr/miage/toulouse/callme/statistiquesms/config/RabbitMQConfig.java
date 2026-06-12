package fr.miage.toulouse.callme.statistiquesms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "callme.events";

    public static final String QUEUE_COURS       = "statistiques.cours";
    public static final String QUEUE_PRESENCE    = "statistiques.presence";
    public static final String QUEUE_COMPETITION = "statistiques.competition";
    public static final String QUEUE_RESULTAT    = "statistiques.resultat";
    public static final String QUEUE_UTILISATEUR = "statistiques.utilisateur";

    @Bean
    public TopicExchange callmeExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean public Queue queueCours()       { return new Queue(QUEUE_COURS, true); }
    @Bean public Queue queuePresence()    { return new Queue(QUEUE_PRESENCE, true); }
    @Bean public Queue queueCompetition() { return new Queue(QUEUE_COMPETITION, true); }
    @Bean public Queue queueResultat()    { return new Queue(QUEUE_RESULTAT, true); }
    @Bean public Queue queueUtilisateur() { return new Queue(QUEUE_UTILISATEUR, true); }

    @Bean public Binding bindingCours(Queue queueCours, TopicExchange callmeExchange) {
        return BindingBuilder.bind(queueCours).to(callmeExchange).with("cours.*");
    }
    @Bean public Binding bindingPresence(Queue queuePresence, TopicExchange callmeExchange) {
        return BindingBuilder.bind(queuePresence).to(callmeExchange).with("presence.*");
    }
    @Bean public Binding bindingCompetition(Queue queueCompetition, TopicExchange callmeExchange) {
        return BindingBuilder.bind(queueCompetition).to(callmeExchange).with("competition.*");
    }
    @Bean public Binding bindingResultat(Queue queueResultat, TopicExchange callmeExchange) {
        return BindingBuilder.bind(queueResultat).to(callmeExchange).with("resultat.*");
    }
    @Bean public Binding bindingUtilisateur(Queue queueUtilisateur, TopicExchange callmeExchange) {
        return BindingBuilder.bind(queueUtilisateur).to(callmeExchange).with("utilisateur.*");
    }

    @Bean
    public MessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
