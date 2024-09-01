package com.ddang.usedauction.answer.controller;

import com.ddang.usedauction.answer.dto.AnswerCreateDto;
import com.ddang.usedauction.answer.dto.AnswerGetDto;
import com.ddang.usedauction.answer.dto.AnswerGetDto.Response;
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

        Response answer = answerService.getAnswer(answerId);

        return ResponseEntity.ok(answer);
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

        Page<Response> answerList = answerService.getAnswerList(principalDetails.getName(),
            pageable);

        return ResponseEntity.ok(answerList);
    }

    /**
     * 답변 생성 컨트롤러
     *
     * @param imageList        이미지
     * @param createDto        답변 생성 정보
     * @param principalDetails 회원 정보
     * @return 성공 시 200 코드와 작성된 답변, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<AnswerGetDto.Response> createAnswerController(
        @RequestPart(required = false) List<@IsImage(message = "올바른 이미지 파일이 아닙니다.") MultipartFile> imageList,
        @Valid @RequestPart
        AnswerCreateDto createDto, @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Response answer = answerService.createAnswer(imageList, createDto,
            principalDetails.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(answer);
    }

    /**
     * 답변 수정 컨트롤러
     *
     * @param answerId         수정할 답변 pk
     * @param imageList        추가할 이미지
     * @param updateDto        수정할 정보
     * @param principalDetails 회원 정보
     * @return 성공 시 200 코드와 수정된 답변, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{answerId}")
    public ResponseEntity<AnswerGetDto.Response> updateAnswerController(
        @NotNull(message = "pk 값은 null 일 수 없습니다.") @Positive(message = "pk 값은 0 또는 음수일 수 없습니다.") @PathVariable Long answerId,
        @RequestPart(required = false) List<@IsImage(message = "올바른 이미지 파일이 아닙니다.") MultipartFile> imageList,
        @Valid @RequestPart AnswerUpdateDto updateDto,
        @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Response answer = answerService.updateAnswer(answerId, imageList, updateDto,
            principalDetails.getName());

        return ResponseEntity.ok(answer);
    }

    /**
     * 회원이 작성한 답변 삭제 컨트롤러
     *
     * @param principalDetails 회원 정보
     * @param answerId         삭제할 답변 pk
     * @return 성공 시 200 코드와 삭제 메시지, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{answerId}")
    public ResponseEntity<String> deleteAnswerController(
        @NotNull(message = "pk값은 null 일 수 없습니다.") @Positive(message = "pk 값은 0 또는 음수일 수 없습니다.") @PathVariable Long answerId,
        @AuthenticationPrincipal PrincipalDetails principalDetails) {

        answerService.deleteAnswer(principalDetails.getName(), answerId);

        return ResponseEntity.ok("삭제되었습니다.");
    }
}
