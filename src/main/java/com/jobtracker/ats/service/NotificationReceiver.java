package com.jobtracker.ats.service;

import com.jobtracker.ats.config.RabbitMQConfig;
import com.jobtracker.ats.dto.NotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationReceiver {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(NotificationMessage message) {
        System.out.println(" [!] Received from Queue. Preparing email for: " + message.getRecipientEmail());
        
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(message.getRecipientEmail());
            email.setSubject(message.getSubject());
            email.setText(message.getBody());
            
            mailSender.send(email);
            
            System.out.println(" [✔] Email sent successfully (Simulated)!");
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}