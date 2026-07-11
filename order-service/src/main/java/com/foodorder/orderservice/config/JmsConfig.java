package com.foodorder.orderservice.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
public class JmsConfig {

    @Value("${activemq.broker-url}")
    private String brokerUrl;

    @Value("${activemq.user}")
    private String username;

    @Value("${activemq.password}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setUserName(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        java.util.Map<String, Class<?>> typeIdMappings = new java.util.HashMap<>();
        typeIdMappings.put("PaymentRequestEvent", com.foodorder.orderservice.dto.PaymentRequestEvent.class);
        typeIdMappings.put("PaymentResponseEvent", com.foodorder.orderservice.dto.PaymentResponseEvent.class);
        typeIdMappings.put("KitchenRequestEvent", com.foodorder.orderservice.dto.KitchenRequestEvent.class);
        typeIdMappings.put("KitchenResponseEvent", com.foodorder.orderservice.dto.KitchenResponseEvent.class);
        typeIdMappings.put("DeliveryRequestEvent", com.foodorder.orderservice.dto.DeliveryRequestEvent.class);
        typeIdMappings.put("DeliveryResponseEvent", com.foodorder.orderservice.dto.DeliveryResponseEvent.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }
}
