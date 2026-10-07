package com.autoservice.identityservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@Configuration
public class ActivationMailConfig {
    @Bean("activationMailSender")
    public JavaMailSender activationMailSender(
            @Value("${app.activation.mail.host:${spring.mail.host:smtp.gmail.com}}") String host,
            @Value("${app.activation.mail.port:${spring.mail.port:587}}") int port,
            @Value("${app.activation.mail.username:${spring.mail.username:${MAIL_USERNAME:}}}") String username,
            @Value("${app.activation.mail.password:${spring.mail.password:${MAIL_PASSWORD:}}}") String password,
            @Value("${app.activation.mail.starttls:true}") boolean tls,
            @Value("${app.activation.mail.ssl:false}") boolean ssl) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host); sender.setPort(port);
        sender.setUsername(username); sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");
        Properties properties = sender.getJavaMailProperties();
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", String.valueOf(tls));
        properties.setProperty("mail.smtp.starttls.required", String.valueOf(tls));
        properties.setProperty("mail.smtp.ssl.enable", String.valueOf(ssl));
        properties.setProperty("mail.smtp.connectiontimeout", "3000");
        properties.setProperty("mail.smtp.timeout", "5000");
        properties.setProperty("mail.smtp.writetimeout", "5000");
        return sender;
    }
}
