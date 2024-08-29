package com.ddang.usedauction.mail.service;

import com.ddang.usedauction.mail.exception.MailDeliveryFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailCheckService {

    private final JavaMailSender mailSender;
    private final MailRedisService mailRedisService;
    private String authNum;

    @Value("${spring.email.email_expiration_time}")
    private long emailExpiration;

    public void sendMessage(String sendMail) {

        authNum = createCode();

        String from = "2sh9735@gmail.com";
        String to = sendMail;
        String title = "[땅땅땅] 이메일 인증 메일입니다.";
        String content =
            "인증번호는 " + authNum + " 입니다.";
        createMessage(from, to, title, content);

    }

    public String createCode() {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        return String.valueOf(1000 + rand.nextInt(9000));
    }

    public void createMessage(String from, String to, String title, String content) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailDeliveryFailedException();
        }

        mailRedisService.setDataExpire(authNum, to, emailExpiration);
    }

    public boolean checkAuthNum(String email, String authNum) {
        return email.equals(mailRedisService.getData(authNum));
    }
}
