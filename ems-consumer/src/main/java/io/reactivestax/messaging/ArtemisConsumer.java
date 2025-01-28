package io.reactivestax.messaging;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ArtemisConsumer {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void consumeMessage(String queueName) {
        try {
            Message message = jmsTemplate.receive(queueName);
            if (message instanceof TextMessage) {
                String text = ((TextMessage) message).getText();
                System.out.println("[X] Received message: " + text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
