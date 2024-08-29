package com.ddang.usedauction.mail.controller;

import com.ddang.usedauction.mail.dto.EmailCheckDto;
import com.ddang.usedauction.mail.dto.EmailSendDto;
import com.ddang.usedauction.mail.service.MailCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mail")
public class MailController {

    private final MailCheckService mailCheckService;

    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestBody @Valid EmailSendDto dto) {
        mailCheckService.sendMessage(dto.getEmail());
        return ResponseEntity.status(HttpStatus.OK)
            .body("인증번호가 전송되었습니다.");
    }

    @PostMapping("/check")
    public ResponseEntity<Boolean> authCheck(@RequestBody @Valid EmailCheckDto dto) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(mailCheckService.checkAuthNum(dto.getEmail(), dto.getAuthNum()));
    }
}
