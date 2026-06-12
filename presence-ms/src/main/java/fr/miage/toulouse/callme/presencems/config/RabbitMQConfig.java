package fr.miage.toulouse.callme.presencems.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE      = "callme.events";
    public static final String QUEUE_COURS   = "presence.cours";
    public static final String KEY_PRESENCE  = "presence.enregistree";

    @Bean
    public Queue queueCours() {
        return new Queue(QUEUE_COURS, true);
    }

    @Bean
    public TopicExchange callmeExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Binding binding(Queue queueCours, TopicExchange callmeExchange) {
        return BindingBuilder.bind(queueCours).to(callmeExchange).with("cours.*");
    }

    @Bean
    public MessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
