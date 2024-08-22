package com.ddang.usedauction.ask.controller;

import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.ask.dto.AskCreateDto;
import com.ddang.usedauction.ask.dto.AskGetDto;
import com.ddang.usedauction.ask.dto.AskUpdateDto;
import com.ddang.usedauction.ask.service.AskService;
import com.ddang.usedauction.security.auth.PrincipalDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asks")
@RequiredArgsConstructor
@Validated
public class AskController {

    private final AskService askService;

    /**
     * 문의 단건 조회 컨트롤러
     *
     * @param askId 문의 pk
     * @return 성공 시 200 코드와 조회된 문의, 실패 시 에러코드와 에러메시지
     */
    @GetMapping("/{askId}")
    public ResponseEntity<AskGetDto.Response> getAskController(
        @NotNull(message = "pk 값은 null 일 수 없습니다.") @Positive(message = "pk 값은 0 또는 음수일 수 없습니다.") @PathVariable Long askId) {

        Ask ask = askService.getAsk(askId);

        return ResponseEntity.ok(AskGetDto.Response.from(ask));
    }

