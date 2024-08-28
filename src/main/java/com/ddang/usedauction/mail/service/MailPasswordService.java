package com.ddang.usedauction.mail.service;

import com.ddang.usedauction.mail.exception.MailDeliveryFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailPasswordService {

    private final JavaMailSender mailSender;

    public void sendMessage(String sendMail, String newPassword) {

        String from = "2sh9735@gmail.com";
        String to = sendMail;
        String title = "[땅땅땅] 임시 비밀번호 안내 메일입니다.";
        String content =
            "임시 비밀번호는 " + newPassword + " 입니다.";
        createMessage(from, to, title, content);

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
    }
}
