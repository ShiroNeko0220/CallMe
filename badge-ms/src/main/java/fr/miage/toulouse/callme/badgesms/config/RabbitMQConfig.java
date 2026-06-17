package fr.miage.toulouse.callme.badgesms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE       = "callme.events";
    public static final String QUEUE_COURS    = "badge.cours.cree";
    public static final String QUEUE_COMPET   = "badge.competition.creee";
    public static final String KEY_COURS      = "cours.cree";
    public static final String KEY_COMPETITION = "competition.creee";

    @Bean
    public TopicExchange callmeExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue queueCours() {
        return QueueBuilder.durable(QUEUE_COURS).build();
    }

    @Bean
    public Queue queueCompetition() {
        return QueueBuilder.durable(QUEUE_COMPET).build();
    }

    @Bean
    public Binding bindingCours(Queue queueCours, TopicExchange callmeExchange) {
        return BindingBuilder.bind(queueCours).to(callmeExchange).with(KEY_COURS);
    }

    @Bean
    public Binding bindingCompetition(Queue queueCompetition, TopicExchange callmeExchange) {
        return BindingBuilder.bind(queueCompetition).to(callmeExchange).with(KEY_COMPETITION);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}