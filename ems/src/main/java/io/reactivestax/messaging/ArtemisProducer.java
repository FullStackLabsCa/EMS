package io.reactivestax.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class ArtemisProducer {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendMessage(String queueName, String message) {
        jmsTemplate.convertAndSend(queueName, message);
        System.out.println("[x] Sent message: " + message);
    }
}
