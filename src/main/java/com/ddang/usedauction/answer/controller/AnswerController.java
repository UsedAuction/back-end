package com.ddang.usedauction.answer.controller;

import com.ddang.usedauction.answer.domain.Answer;
import com.ddang.usedauction.answer.dto.AnswerCreateDto;
import com.ddang.usedauction.answer.dto.AnswerGetDto;
import com.ddang.usedauction.answer.dto.AnswerUpdateDto;
import com.ddang.usedauction.answer.service.AnswerService;
import com.ddang.usedauction.security.auth.PrincipalDetails;
import com.ddang.usedauction.validation.IsImage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
@Validated
public class AnswerController {

    private final AnswerService answerService;

    /**
     * 답변 단건 조회 컨트롤러
     *
     * @param answerId 답변 pk
     * @return 성공 시 200 코드와 조회된 답변, 실패 시 에러코드와 에러메시지
     */
    @GetMapping("/{answerId}")
    public ResponseEntity<AnswerGetDto.Response> getAnswerController(
        @NotNull(message = "pk 값은 null 일 수 없습니다.") @Positive(message = "pk 값은 0 또는 음수일 수 없습니다.") @PathVariable Long answerId) {

        Answer answer = answerService.getAnswer(answerId);

        return ResponseEntity.ok(AnswerGetDto.Response.from(answer));
    }

    /**
     * 회원이 작성한 답변 리스트 조회
     *
     * @param principalDetails 회원 정보
     * @param pageable         페이징
     * @return 성공 시 200 코드와 페이징된 답변 리스트, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<Page<AnswerGetDto.Response>> getAnswerListController(
        @AuthenticationPrincipal
        PrincipalDetails principalDetails,
        @PageableDefault(sort = "createdAt", direction = Direction.DESC)
        Pageable pageable) {

        Page<Answer> answerPageList = answerService.getAnswerList(principalDetails.getName(),
            pageable);

        return ResponseEntity.ok(answerPageList.map(AnswerGetDto.Response::from));
    }

