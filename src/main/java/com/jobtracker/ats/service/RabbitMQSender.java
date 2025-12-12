package com.jobtracker.ats.service;

import com.jobtracker.ats.config.RabbitMQConfig;
import com.jobtracker.ats.dto.NotificationMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendNotification(NotificationMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
        System.out.println(" [x] Sent to Queue: " + message.getRecipientEmail());
    }
}