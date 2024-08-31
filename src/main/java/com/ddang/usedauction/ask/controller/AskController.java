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

    /**
     * 회원이 작성한 문의 리스트 조회 컨트롤러
     *
     * @param principalDetails 회원 정보
     * @param pageable         페이징
     * @return 성공 시 200 코드와 페이징된 문의 리스트, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<Page<AskGetDto.Response>> getAskList(@AuthenticationPrincipal
    PrincipalDetails principalDetails,
        @PageableDefault(sort = "createdAt", direction = Direction.DESC)
        Pageable pageable) {

        Page<Ask> askPageList = askService.getAskList(principalDetails.getName(), pageable);

        return ResponseEntity.ok(askPageList.map(AskGetDto.Response::from));
    }

    /**
     * 회원이 받은 문의 리스트 조회
     *
     * @param principalDetails 회원 정보
     * @param pageable         페이징
     * @return 성공 시 200 코드와 페이징된 문의 리스트, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/receive")
    public ResponseEntity<Page<AskGetDto.Response>> getReceiveAskList(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {

        Page<Ask> askList = askService.getReceiveAskList(principalDetails.getName(),
            pageable);

        return ResponseEntity.ok(askList.map(AskGetDto.Response::from));
    }

    /**
     * 문의 생성 컨트롤러
     *
     * @param createDto        문의 정보
     * @param principalDetails 회원 정보
     * @return 성공 시 200 코드와 생성된 문의, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<AskGetDto.Response> createAskController(
        @Valid @RequestBody AskCreateDto createDto,
        @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Ask ask = askService.createAsk(createDto, principalDetails.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AskGetDto.Response.from(ask));
    }

    /**
     * 문의 수정 컨트롤러
     *
     * @param askId            수정할 문의 pk
     * @param updateDto        수정 정보
     * @param principalDetails 회원 정보
     * @return 성공 시 200 코드와 수정된 문의, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{askId}")
    public ResponseEntity<AskGetDto.Response> updateAskController(
        @NotNull(message = "pk 값은 null 일 수 없습니다.") @Positive(message = "pk 값은 0 또는 음수일 수 없습니다.") @PathVariable Long askId,
        @Valid @RequestBody
        AskUpdateDto updateDto, @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Ask ask = askService.updateAsk(askId, updateDto, principalDetails.getName());

        return ResponseEntity.ok(AskGetDto.Response.from(ask));
    }

    /**
     * 회원이 작성한 문의 삭제 컨트롤러
     *
     * @param principalDetails 회원 정보
     * @return 성공 시 200 코드와 삭제메시지, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping
    public ResponseEntity<String> deleteAskController(
        @AuthenticationPrincipal PrincipalDetails principalDetails) {

        askService.deleteAsk(principalDetails.getName());

        return ResponseEntity.ok("삭제되었습니다.");
    }
}
