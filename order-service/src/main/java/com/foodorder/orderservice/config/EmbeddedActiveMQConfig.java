package com.foodorder.orderservice.config;

import org.apache.activemq.broker.BrokerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedActiveMQConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService brokerService() throws Exception {
        BrokerService broker = new BrokerService();
        broker.setBrokerName("embedded-activemq-broker");
        broker.setPersistent(false);
        broker.setUseJmx(true);
        // Start TCP connector on port 61616 for external microservices to connect
        broker.addConnector("tcp://localhost:61616");
        // Start VM connector for local connection
        broker.addConnector("vm://localhost");
        return broker;
    }
}
