package com.ddang.usedauction.member.component;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MailComponent {
    private final JavaMailSender javaMailSender;

    public boolean send(String fromEmail, String fromName, String toEmail, String toName, String title, String contents) {
        boolean result = false;

        MimeMessagePreparator mimeMessagePreparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                InternetAddress from = new InternetAddress();
                from.setAddress(fromEmail);
                from.setPersonal(fromName);

                InternetAddress to = new InternetAddress();
                to.setAddress(toEmail);
                to.setPersonal(toName);

                mimeMessageHelper.setFrom(from);
                mimeMessageHelper.setTo(to);
                mimeMessageHelper.setSubject(title);
                mimeMessageHelper.setText(contents, true);
            }
        };

        try {
            javaMailSender.send(mimeMessagePreparator);
            result = true;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return result;
    }
}

