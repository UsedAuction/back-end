package com.ddang.usedauction.auction.controller;

import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.dto.AuctionCreateDto;
import com.ddang.usedauction.auction.dto.AuctionGetDto;
import com.ddang.usedauction.auction.dto.AuctionGetDto.Response;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.security.auth.PrincipalDetails;
import com.ddang.usedauction.validation.IsImage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
@Validated
public class AuctionController {

    private final AuctionService auctionService;

    /**
     * 경매글 단건 조회 컨트롤러
     *
     * @param auctionId 조회할 경매글 PK
     * @return 성공 시 200 코드와 조회된 경매글 정보, 실패 시 에러코드와 에러메시지
     */
    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionGetDto.Response> getAuctionController(
        @Positive(message = "PK값은 0 또는 음수일 수 없습니다.") @PathVariable Long auctionId) {

        Response response = auctionService.getAuction(auctionId);

        return ResponseEntity.ok(response);
    }

    /**
     * 경매글 리스트 조회 컨트롤러
     *
     * @param word         검색어
     * @param mainCategory 대분류 카테고리
     * @param subCategory  소분류 카테고리
     * @param sorted       정렬 방법
     * @return 성공 시 200 코드와 페이징 처리된 경매글 리스트, 실패 시 에러코드와 에러메시지
     */
    @GetMapping
    public ResponseEntity<Page<AuctionGetDto.Response>> getAuctionListController(
        @RequestParam(required = false) String word,
        @RequestParam(required = false) String mainCategory,
        @RequestParam(required = false) String subCategory,
        @RequestParam(required = false) String sorted,
        @PageableDefault Pageable pageable) {

        Page<Response> auctionList = auctionService.getAuctionList(word, mainCategory, subCategory,
            sorted,
            pageable);

        return ResponseEntity.ok(auctionList);
    }

    /**
     * top5 경매 리스트 조회 컨트롤러
     *
     * @return 조회된 경매 리스트
     */
    @GetMapping("/top5")
    public ResponseEntity<List<AuctionGetDto.Response>> getTop5Controller(
        @RequestParam(required = false) String mainCategory,
        @RequestParam(required = false) String subCategory) {

        List<Response> top5 = auctionService.getTop5(mainCategory, subCategory);

        return ResponseEntity.ok(top5);
    }

    /**
     * 경매글 생성 컨트롤러
     *
     * @param thumbnail        대표 이미지
     * @param imageList        추가 이미지 리스트
     * @param createDto        경매글 작성 정보
     * @param principalDetails 회원 정보
     * @return 성공 시 201코드와 작성된 경매글의 PK와 제목, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<AuctionCreateDto.Response> createAuctionController(
        @IsImage(message = "이미지 파일이 아니거나 올바르지 않은 이미지 입니다. (허용하는 확장자 : .jpg, .jpeg, .png)") @RequestPart
        MultipartFile thumbnail,
        @RequestPart(required = false) List<@IsImage(message = "이미지 파일이 아니거나 올바르지 않은 이미지 입니다. (허용하는 확장자 : .jpg, .jpeg, .png)") MultipartFile> imageList,
        @Valid @RequestPart AuctionCreateDto.Request createDto, @AuthenticationPrincipal
    PrincipalDetails principalDetails) {

        String memberEmail = principalDetails.getName();

        AuctionCreateDto.Response response = auctionService.createAuction(thumbnail, imageList,
            memberEmail,
            createDto);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(response);
    }

    /**
     * 구매 확정 컨트롤러
     *
     * @param auctionId        경매글 PK
     * @param confirmDto       구매 확정 정보
     * @param principalDetails 회원 정보
     * @return 성공 시 200 코드, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{auctionId}/confirm")
    public ResponseEntity<String> confirmAuctionController(
        @Positive(message = "PK값은 0 또는 음수일 수 없습니다.") @PathVariable Long auctionId,
        @Valid @RequestBody
        AuctionConfirmDto.Request confirmDto,
        @AuthenticationPrincipal PrincipalDetails principalDetails) {

        String memberEmail = principalDetails.getName();

        auctionService.confirmAuction(auctionId, memberEmail, confirmDto);

        return ResponseEntity.ok("구매 확정 완료");
    }

    /**
     * 즉시 구매 컨트롤러
     *
     * @param auctionId        즉시 구매할 경매글 PK
     * @param principalDetails 회원 정보
     * @return 성공 시 200 코드, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{auctionId}")
    public ResponseEntity<String> instantPurchaseAuctionController(
        @Positive(message = "PK값은 0 또는 음수일 수 없습니다.") @PathVariable Long auctionId,
        @AuthenticationPrincipal PrincipalDetails principalDetails) {

        String memberEmail = principalDetails.getName();

        auctionService.instantPurchaseAuction(auctionId, memberEmail);

        return ResponseEntity.ok("즉시 구매 성공");
    }
}
